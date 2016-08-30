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

import org.apache.spark.mllib.rdd.RDDFunctions._
import org.apache.spark.rdd.RDD
import org.dia.algorithms.mcc.GTGRunner
import org.dia.core.SciDataset

class GTGRunnerReduceByKey(override val masterURL: String,
                          override val paths: String,
                          override val varName: String,
                          override val partitions: Int)
  extends GTGRunner(masterURL, paths, varName, partitions) {

  override def pairConsecutiveFrames(sRDD: RDD[SciDataset]): RDD[(SciDataset, SciDataset)] = {
    val k = sRDD.sortBy(p => p.attr("FRAME").toInt, true, org.dia.macrobench.core.BenchmarkContext.partitionCount)
      .zipWithIndex()
      .flatMap({ case (sciD, indx) => List((indx, List(sciD)), (indx + 1, List(sciD))) })
      .reduceByKey(_ ++ _)
      .filter({ case (_, sciDs) => sciDs.size == 2 })
      .map({ case (_, sciDs) => sciDs.toList.sortBy(p => p.attr("FRAME").toInt) })
      .map(sciDs => (sciDs(0), sciDs(1)))
  }

}
