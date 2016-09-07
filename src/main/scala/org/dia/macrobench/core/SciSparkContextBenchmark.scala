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

import org.dia.core.SciSparkContext


@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
class SciSparkContextBenchmark {

  @Param(Array("100gb/", "200gb/", "300gb/", "400gb/", "500gb/", "1000gb/", "1500gb/", "2000gb/", "2500gb/", "3000gb"))
  var directory : String = _

  var bcont : BenchmarkContext = _
  var sc: SciSparkContext = _
  var fspath: String = _

  @Setup(Level.Iteration)
  def init(): Unit = {
    bcont = new BenchmarkContext()
    sc = bcont.sc
    fspath = bcont.fspath
  }

  @TearDown(Level.Iteration)
  def destroy(): Unit = {
    sc.sparkContext.stop()
  }

//  @Benchmark
//  def SciDatasets(): Long = {
//    sc.sciDatasets(fspath + directory, List("ch4"), bcont.partitionCount).count()
//  }

  @Benchmark
  def NetcdfDFSFiles(): Long = {
    sc.netcdfDFSFiles(fspath + directory, List("square"), bcont.partitionCount).count()
  }

  @Benchmark
  def NetcdfRandomAccessDatasets(): Long = {
    sc.netcdfRandomAccessDatasets(fspath + directory, List("square"), bcont.partitionCount).count()
  }

  @Benchmark
  def NetcdfDFSDatasets(): Long = {
    sc.netcdfDFSDatasets(fspath + directory, List("square"), bcont.partitionCount).count()
  }

  @Benchmark
  def TwoVarsNetcdfDFSFiles(): Long = {
    sc.netcdfDFSFiles(fspath + directory, List("square", "cube"), bcont.partitionCount).count()
  }

  @Benchmark
  def TwoVarsNetcdfRandomAccessDatasets(): Long = {
    sc.netcdfRandomAccessDatasets(fspath + directory, List("square", "cube"), bcont.partitionCount).count()
  }

  @Benchmark
  def TwoVarsNetcdfDFSDatasets(): Long = {
    sc.netcdfDFSDatasets(fspath + directory, List("square", "cube"), bcont.partitionCount).count()
  }

  @Benchmark
  def AllVarsNetcdfDFSFiles(): Long = {
    sc.netcdfDFSFiles(fspath + directory, List("vector", "square", "cube", "hyperCube"), bcont.partitionCount).count()
  }

  @Benchmark
  def AllVarsNetcdfRandomAccessDatasets(): Long = {
    sc.netcdfRandomAccessDatasets(fspath + directory, Nil, bcont.partitionCount).count()
  }

  @Benchmark
  def AllVarsNetcdfDFSDatasets(): Long = {
    sc.netcdfDFSDatasets(fspath + directory, Nil, bcont.partitionCount).count()
  }
}
