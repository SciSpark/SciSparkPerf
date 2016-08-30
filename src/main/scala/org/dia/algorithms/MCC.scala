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

package org.dia.algorithms

import scala.collection.mutable

import org.apache.spark.mllib.rdd.RDDFunctions._

import org.dia.algorithms.mcc.{GTGRunner, MCCOps}
import org.dia.core.SciSparkContext

object MCC {

  val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  def runGroupByKeyMCC(sc: SciSparkContext, path: String): Unit = {
    val runner = new GTGRunnerGroupByKey("doesn'tmatter", path, "ch4", 1)
    val sRDD = sc.sciDatasets(path, List("ch4"))
    /**
     * Collect lat and lon arrays
     */
    val sampleDataset = sRDD.take(1)(0)
//    val lon = sampleDataset("longitude").data()
//    val lat = sampleDataset("latitude").data()

    /**
     * Record the frame Number in each SciTensor
     */
    val labeled = runner.recordFrameNumber(sRDD, "ch4")

    /**
     * Filter for temperature values under 241.0
     */
    val filtered = labeled.map(p => p("ch4") = p("ch4") <= 241.0)

    /**
     * Pair consecutive frames
     */
    val consecFrames = runner.pairConsecutiveFrames(filtered)

    /**
     * Core MCC
     */
    val edgeListRDD = runner.findEdges(consecFrames,
      "ch4",
      runner.maxAreaOverlapThreshold,
      runner.minAreaOverlapThreshold,
      runner.convectiveFraction,
      runner.minArea,
      runner.nodeMinArea)

    /**
     * Collect the edgeList and construct NodeMap
     */
    val MCCEdgeList = edgeListRDD.collect()
//    val MCCNodeMap = runner.createNodeMapFromEdgeList(MCCEdgeList, lat, lon)
//
//    logger.info("NUM VERTICES : " + MCCNodeMap.size + "\n")
//    logger.info("NUM EDGES : " + MCCEdgeList.size + "\n")
  }


  def runSortedSlidingMCC(sc: SciSparkContext, path: String): Unit = {
    val runner = new GTGRunnerSliding("doesn'tmatter", path, "ch4", 1)
    val sRDD = sc.sciDatasets(path, List("ch4"))
    /**
     * Collect lat and lon arrays
     */
    val sampleDataset = sRDD.take(1)(0)
//    val lon = sampleDataset("longitude").data()
//    val lat = sampleDataset("latitude").data()

    /**
     * Record the frame Number in each SciTensor
     */
    val labeled = runner.recordFrameNumber(sRDD, "ch4")

    /**
     * Filter for temperature values under 241.0
     */
    val filtered = labeled.map(p => p("ch4") = p("ch4") <= 241.0)

    /**
     * Pair consecutive frames
     */
    val consecFrames = runner.pairConsecutiveFrames(filtered)

    /**
     * Core MCC
     */
    val edgeListRDD = runner.findEdges(consecFrames,
      "ch4",
      runner.maxAreaOverlapThreshold,
      runner.minAreaOverlapThreshold,
      runner.convectiveFraction,
      runner.minArea,
      runner.nodeMinArea)

    /**
     * Collect the edgeList and construct NodeMap
     */
    val MCCEdgeList = edgeListRDD.collect()
//    val MCCNodeMap = runner.createNodeMapFromEdgeList(MCCEdgeList, lat, lon)
//
//    logger.info("NUM VERTICES : " + MCCNodeMap.size + "\n")
//    logger.info("NUM EDGES : " + MCCEdgeList.size + "\n")
  }

  def runReduceByKeyMCC(sc: SciSparkContext, path: String): Unit = {
    val runner = new GTGRunner("doesn'tmatter", path, "ch4", 1)
    val sRDD = sc.sciDatasets(path, List("ch4"))
    /**
     * Collect lat and lon arrays
     */
    val sampleDataset = sRDD.take(1)(0)
//    val lon = sampleDataset("longitude").data()
//    val lat = sampleDataset("latitude").data()

    /**
     * Record the frame Number in each SciTensor
     */
    val labeled = runner.recordFrameNumber(sRDD, "ch4")

    /**
     * Filter for temperature values under 241.0
     */
    val filtered = labeled.map(p => p("ch4") = p("ch4") <= 241.0)

    /**
     * Pair consecutive frames
     */
    val consecFrames = runner.pairConsecutiveFrames(filtered)

    /**
     * Core MCC
     */
    val edgeListRDD = runner.findEdges(consecFrames,
      "ch4",
      runner.maxAreaOverlapThreshold,
      runner.minAreaOverlapThreshold,
      runner.convectiveFraction,
      runner.minArea,
      runner.nodeMinArea)

    /**
     * Collect the edgeList and construct NodeMap
     */
    val MCCEdgeList = edgeListRDD.collect()
//    val MCCNodeMap = runner.createNodeMapFromEdgeList(MCCEdgeList, lat, lon)
//
//    logger.info("NUM VERTICES : " + MCCNodeMap.size + "\n")
//    logger.info("NUM EDGES : " + MCCEdgeList.size + "\n")
  }

}
