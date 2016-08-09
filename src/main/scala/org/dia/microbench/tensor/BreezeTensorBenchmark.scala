package org.dia.microbench.tensor

import org.dia.tensors.BreezeTensor


class BreezeTensorBenchmark extends TensorBenchmarkTemplate {

  override def setup: Unit = {
    a = new BreezeTensor(array, Array(dim, dim))
    b = new BreezeTensor(array, Array(dim, dim))
  }

  override def instantiate: BreezeTensor = new BreezeTensor(array, Array(dim, dim))
}
