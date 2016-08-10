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

package org.dia.microbench.tensor

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._

import org.dia.tensors.AbstractTensor

@BenchmarkMode(Array(Mode.All))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
abstract class TensorBenchmarkTemplate extends InstantiateBenchmarkTemplate {

  @Param(Array("500", "1000", "1500", "2000", "2500"))
  var dim: Int = _

  var a : AbstractTensor = _
  var b : AbstractTensor = _
  var array : Array[Double] = _

  @Setup
  def init(): Unit = {
    array = (0d to dim * dim by 1d).toArray
  }

  @Setup
  def setup(): Unit


  @Benchmark
  def element_wise_sub: AbstractTensor = a - b

  @Benchmark
  def matrix_wise_dot: AbstractTensor = a ** b

  @Benchmark
  def java_loop_mask: AbstractTensor = a <= 241

}

trait InstantiateBenchmarkTemplate {

  @Benchmark
  def instantiate: AbstractTensor
}