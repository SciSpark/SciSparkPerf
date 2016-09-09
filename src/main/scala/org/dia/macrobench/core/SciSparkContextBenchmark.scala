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

import org.dia.core.SciDataset


@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
class SciSparkContextBenchmark {

  @Param(Array("100gb/", "200gb/", "300gb/", "400gb/", "500gb/", "1000gb/", "2000gb/", "2500gb/", "3000gb"))
  var directory : String = _

  val bcont = BenchmarkContext
  var ssc = bcont.sc
  var fspath: String = bcont.fspath

  var rdd : RDD[SciDataset] = _

  @TearDown(Level.Iteration)
  def destroy(): Unit = {
    rdd.unpersist(true)
  }

  @Benchmark
  def NetcdfRandomAccessDatasets(): Array[Unit] = {
    rdd = ssc.netcdfRandomAccessDatasets(fspath + directory, List("square"), bcont.partitionCount)
    bcont.evaluate(rdd)
  }

  @Benchmark
  def NetcdfDFSDatasets(): Array[Unit] = {
    rdd = ssc.netcdfDFSDatasets(fspath + directory, List("square"), bcont.partitionCount)
    bcont.evaluate(rdd)
  }


  @Benchmark
  def TwoVarsNetcdfRandomAccessDatasets(): Array[Unit] = {
    rdd = ssc.netcdfRandomAccessDatasets(fspath + directory, List("square", "cube"), bcont.partitionCount)
    bcont.evaluate(rdd)
  }

  @Benchmark
  def TwoVarsNetcdfDFSDatasets(): Array[Unit] = {
    rdd = ssc.netcdfDFSDatasets(fspath + directory, List("square", "cube"), bcont.partitionCount)
    bcont.evaluate(rdd)
  }


  @Benchmark
  def AllVarsNetcdfRandomAccessDatasets(): Array[Unit] = {
    rdd = ssc.netcdfRandomAccessDatasets(fspath + directory, Nil, bcont.partitionCount)
    bcont.evaluate(rdd)
  }

  @Benchmark
  def AllVarsNetcdfDFSDatasets(): Array[Unit] = {
    rdd = ssc.netcdfDFSDatasets(fspath + directory, Nil, bcont.partitionCount)
    bcont.evaluate(rdd)
  }
}
