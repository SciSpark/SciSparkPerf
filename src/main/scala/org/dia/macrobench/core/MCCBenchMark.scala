/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dia.macrobench.core

import java.util.concurrent.TimeUnit

import scala.collection.mutable

import org.openjdk.jmh.annotations._

import org.dia.algorithms.mcc.MCCOps
import org.dia.core.SciSparkContext

@BenchmarkMode(Array(Mode.Throughput, Mode.AverageTime))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
class MCCBenchMark {
  @Param(Array("100mb", "1gb", "10gb", "100gb"))
  var directory : String = _

  var sc : SciSparkContext = _
  var fspath : String = _

  @Setup
  def setup() : Unit = {
    val bsc = new BenchmarkContext()
    sc = bsc.sc
    fspath = bsc.fspath

  }

  @TearDown
  def teardown() : Unit = sc.sparkContext.stop()

  @Benchmark
  def runMCC: Array[((String, Double), (String, Double), Int)] = {
    val variable = "ch4"
    val maxAreaOverlapThreshold = 0.66
    val minAreaOverlapThreshold = 0.50
    val minArea = 10000

    /**
     * Ingest the input file and construct the SRDD.
     * For MCC the sources are used to map date-indexes.
     * The metadata variable "FRAME" corresponds to an index.
     * The indices themselves are numbered with respect to
     * date-sorted order.
     *
     * Note if no HDFS path is given, then randomly generated matrices are used.
     *
     */
    val sRDD = sc.NetcdfDFSFile(fspath + directory, List("ch4"))
    val labeled = sRDD.map(p => {
      val source = p.metaData("SOURCE").split("/").last.split("_")(1)
      val FrameID = source.toInt
      p.insertDictionary(("FRAME", FrameID.toString))
      // need to reshape to a 2d array
      p.insertVar(p.varInUse, p.tensor.reshape(Array(p.shape(1), p.shape(2))))
      p
    })

    /**
     * The MCC algorithm : Mining for graph vertices and edges
     *
     * For each array N* where N is the frame number and N* is the array
     * output the following pairs (N, N*), (N + 1, N*).
     *
     * After flat-mapping the pairs and applying additional pre-processing
     * we have pairs (X, Y) where X is a label and Y a tensor.
     *
     * After grouping by X and reordering pairs we obtain pairs
     * (N*, (N+1)*) which achieves the consecutive pairwise grouping
     * of frames.
     */
    val filtered = labeled.map(p => p(variable) <= 241.0)
    val consecFrames = filtered.flatMap(p => {
      List((p.metaData("FRAME").toInt, p), (p.metaData("FRAME").toInt + 1, p))
    }).groupBy(_._1)
      .map(p => p._2.map(e => e._2).toList)
      .filter(p => p.size > 1)
      .map(p => p.sortBy(_.metaData("FRAME").toInt))
      .map(p => (p(0), p(1)))

    val debug = consecFrames.collect()
    /**
     * Core MCC
     * For each consecutive frame pair, find it's components.
     * For each component pairing, find if the element-wise
     * component pairing results in a zero matrix.
     * If not output a new edge pairing of the form ((Frame, Component), (Frame, Component))
     */
    val componentFrameRDD = consecFrames.flatMap({
      case (t1, t2) =>
        /**
         * First label the connected components in each pair.
         * The following example illustrates labeling.
         *
         * [0,1,2,0]       [0,1,1,0]
         * [1,2,0,0]   ->  [1,1,0,0]
         * [0,0,0,1]       [0,0,0,2]
         *
         * Note that a tuple of (Matrix, MaxLabel) is returned
         * to denote the labeled elements and the highest label.
         * This way only one traverse is necessary instead of a 2nd traverse
         * to find the highest label.
         */
        val (components1, _) = MCCOps.labelConnectedComponents(t1.tensor)
        val (components2, _) = MCCOps.labelConnectedComponents(t2.tensor)
        /**
         * The labeled components are element-wise multiplied
         * to find overlapping regions. Non-overlapping regions
         * result in a 0.
         *
         * [0,1,1,0]       [0,1,1,0]     [0,1,1,0]
         * [1,1,0,0]   X   [2,0,0,0]  =  [2,0,0,0]
         * [0,0,0,2]       [0,0,0,3]     [0,0,0,6]
         *
         */
        val product = components1 * components2
        /**
         * The overlappedPairsList keeps track of all points that
         * overlap between the labeled arrays. Note that the overlappedPairsList
         * will have several duplicate pairs if there are large regions of overlap.
         *
         * This is achieved by iterating through the product array and noting
         * all points that are not 0.
         */
        var overlappedPairsList = mutable.MutableList[((Double, Double))]()
        /**
         * The areaMinMaxTable keeps track of the area, minimum value, and maximum value
         * of all labeled regions in both components. For this reason the hash key has the following form :
         * 'F : C' where F = Frame Number and C = Component Number.
         * The areaMinMaxTable is updated by the updateComponent function, which is called in the for loop.
         *
         * @todo Extend it to have a lat long bounds for component.
         */
        var areaMinMaxTable = new mutable.HashMap[String, (Double, Double, Double)]

        def updateComponent(label: Double, frame: String, value: Double): Unit = {

          if (label != 0.0) {
            var area = 0.0
            var max = Double.MinValue
            var min = Double.MaxValue
            val currentProperties = areaMinMaxTable.get(frame + ":" + label)
            if (currentProperties != null && currentProperties.isDefined) {
              area = currentProperties.get._1
              max = currentProperties.get._2
              min = currentProperties.get._3
              if (value < min) min = value
              if (value > max) max = value
            } else {
              min = value
              max = value
            }
            area += 1
            areaMinMaxTable += ((frame + ":" + label, (area, max, min)))
          }
        }

        for (row <- 0 until product.rows) {
          for (col <- 0 until product.cols) {
            /** Find non-zero points in product array */
            if (product(row, col) != 0.0) {
              /** save components ids */
              val value1 = components1(row, col)
              val value2 = components2(row, col)
              overlappedPairsList += ((value1, value2))
            }
            updateComponent(components1(row, col), t1.metaData("FRAME"), t1.tensor(row, col))
            updateComponent(components2(row, col), t2.metaData("FRAME"), t2.tensor(row, col))
          }
        }

        /**
         * This code essetially computes the number of times the same tuple occurred in the list,
         * a repeat occurrance would indicate that the components overlapped for more than one cell
         * in the product matrix. By calculating the number of overlaps we can calculate the number of cells
         * they overlapped for and since each cell is of a fixed area we can compute the area overlap
         * between those two components.
         */
        var overlappedMap = overlappedPairsList.groupBy(identity).mapValues(_.size)
        // println(s"Overlap Map ${overlappedMap.size}")

        /**
         * Once the overlapped pairs have been computed, eliminate all duplicates
         * by converting the collection to a set. The component edges are then
         * mapped to the respective frames, so the global space of edges (outside of this task)
         * consists of unique tuples.
         */
        val edgesSet = overlappedPairsList.toSet
        // println(s"Overlap SEt ${edgesSet.size}  : ") // for debugging

        val edges = edgesSet.map({ case (c1, c2) => ((t1.metaData("FRAME"), c1), (t2.metaData("FRAME"), c2)) })
        // println(s"Edges ${edges.size} ") // for debugging
      val filtered = edges.filter({
        case ((frameId1, compId1), (frameId2, compId2)) =>
          val (area1, max1, min1) = areaMinMaxTable(frameId1 + ":" + compId1)
          val isCloud1 = ((area1 >= 2400.0) || ((area1 < 2400.0) && ((min1/max1) < 0.9)))
          val (area2, max2, min2) = areaMinMaxTable(frameId2 + ":" + compId2)
          val isCloud2 = ((area2 >= 2400.0) || ((area2 < 2400.0) && ((min2/max2) < 0.9)))
          var meetsCriteria = true
          if(isCloud1 && isCloud2) {
            val areaOverlap = overlappedMap.get(compId1, compId2).get
            val percentAreaOverlap = math.max((areaOverlap / area1), (areaOverlap / area2))
            var edgeWeight = 0
            if (percentAreaOverlap >= maxAreaOverlapThreshold) {
              edgeWeight = 1
            }
            else if (percentAreaOverlap < maxAreaOverlapThreshold &&
              percentAreaOverlap >= minAreaOverlapThreshold) {
              edgeWeight = 2
            }
            else if (areaOverlap >= minArea) {
              edgeWeight = 3
            }
            else {
              meetsCriteria = false
            }
            overlappedMap += (((compId1, compId2), 1))
          }
          isCloud1 && isCloud2 && meetsCriteria
      })

        val edgeList = new mutable.HashSet[((String, Double), (String, Double), Int)]()
        filtered.foreach(edge => {
          val key = (edge._1._2, edge._2._2)
          if(overlappedMap.contains(key)) {
            edgeList += ((edge._1, edge._2, overlappedMap(key)))
          }
        })
//        println(s"edgeList Map filetered ${edgeList.size}: $edgeList")
//        println(s"filtered Map ${filtered.size}")
        edgeList
    })

    /**
     * Collect the edges of the form ((String, Double), (String, Double))
     * From the edges collect all used vertices.
     * Repeated vertices are eliminated due to the set conversion.
     *
     * @todo Make a vertex be of the form
     * ((frameId, componentId), area, min, max)
     * to also store area, min, max at the end of MCC.
     */
    componentFrameRDD.collect()
  }

}
