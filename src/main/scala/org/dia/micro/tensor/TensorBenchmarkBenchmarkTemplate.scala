package org.dia.micro.tensor

import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

import org.dia.tensors.AbstractTensor

@BenchmarkMode(Array(Mode.All))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
abstract class TensorBenchmarkTemplate extends InstantiateBenchmarkTemplate {

  @Param(Array("500", "1000", "1500", "2000", "2500", "3000", "3500", "4000"))
  var dim: Int = _

  var a : AbstractTensor = _
  var b : AbstractTensor = _
  var array : Array[Double] = _

  @Setup
  def init() = {
    array = (0d to dim * dim by 1d).toArray
  }

  @Setup
  def setup():  Unit



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