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

import org.openjdk.jmh.annotations._

import org.apache.spark.rdd.RDD

import org.dia.algorithms.mcc.MCCEdge

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
class MCCBenchMark {
  @Param(Array("100gb/", "200gb/", "300gb/", "400gb/", "500gb/", "1000gb/", "2000gb/"))
  var directory: String = _

  val bcont = BenchmarkContext
  var ssc = bcont.sc
  var fspath: String = bcont.fspath
  var edgeRDD : RDD[MCCEdge] = _

  @TearDown(Level.Iteration)
  def destroy(): Unit = {
    edgeRDD.unpersist(true)
  }

//  @Benchmark
//  def runSlidingMCC(): Unit = {
//    org.dia.algorithms.MCC.runSortedSlidingMCC(ssc, fspath + directory, bcont.partitionCount)
//  }
//
//  @Benchmark
//  def runGroupByKeyMCC(): Unit = {
//    org.dia.algorithms.MCC.runGroupByKeyMCC(ssc, fspath + directory, bcont.partitionCount)
//  }

  @Benchmark
  def runReduceByKeyMCC(): Unit = {
    edgeRDD = org.dia.algorithms.MCC.runReduceByKeyMCC(ssc, fspath + directory, bcont.partitionCount, "cube")
    bcont.evaluate(edgeRDD)
  }
}
