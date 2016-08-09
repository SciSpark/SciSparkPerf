package org.dia.macrobench.core

import java.util.concurrent.TimeUnit

import org.apache.spark.{SparkConf, SparkContext}
import org.dia.core.SciSparkContext
import org.openjdk.jmh.annotations._


@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
class SciSparkContextBenchmark {

  val properties = scala.io.Source.fromFile("Properties").mkString.split("\n")

  var hdfsPath = properties(0)
  var contextURI = "spark://" + properties(1) + ":7077"

  @Param(Array("100mb"))//, "1gb", "10gb", "100gb"))
  var directory : String = _

  var sc : SciSparkContext = new SciSparkContext(new SparkContext(new SparkConf().setMaster("local[*]").setAppName("SciSparkContextBenchmark")))

  @Benchmark
  def readDFS: Long = {
    sc.NetcdfDFSFile(hdfsPath + directory, List("T2M")).count()
  }

}
