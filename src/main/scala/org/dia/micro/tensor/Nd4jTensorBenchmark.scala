package org.dia.micro.tensor

import org.dia.tensors.Nd4jTensor

class Nd4jTensorBenchmark extends TensorBenchmarkTemplate {

  override def setup: Unit = {
    a = new Nd4jTensor(array, Array(dim, dim))
    b = new Nd4jTensor(array, Array(dim, dim))
  }

  override def instantiate: Nd4jTensor = new Nd4jTensor(array, Array(dim, dim))
}