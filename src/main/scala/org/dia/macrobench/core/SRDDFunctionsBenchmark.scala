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

import org.apache.spark.rdd.RDD
import org.dia.core.{SciDataset, SciSparkContext, Variable}
import org.dia.core.SRDDFunctions._
import org.openjdk.jmh.annotations._

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
class SRDDFunctionsBenchmark {

  @Param(Array("1gb/", "10gb/", "100gb/", "1000gb/"))
  var directory : String = _

  var sc : SciSparkContext = _
  var fspath : String = _

  var srdd : RDD[SciDataset] = _
  @Setup
  def setup() : Unit = {
    val bsc = new BenchmarkContext()
    sc = bsc.sc
    fspath = bsc.fspath
    srdd = sc.sciDatasets(fspath + directory, List("ch4"))
      .map(p => p("FRAME") = p.datasetName.split("_")(1))
  }

  @TearDown
  def teardown() : Unit = sc.sparkContext.stop()

  @Benchmark
  def repartitionBySpace(): RDD[Variable] = {
    srdd.repartitionBySpace("ch4", p => p.attr("FRAME").toInt, 20, 20)
  }
}
