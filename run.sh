#!/usr/bin/env bash
export MESOS_NATIVE_JAVA_LIBRARY=""
export SPARK_EXECUTOR_URI=""
sbt clean
sbt assembly
sbt "jmh:run .*macrobench*. -rf csv -rff MacroBenchmark.csv"
sbt "jmh:run .*microbench*. -rf csv -rff MicroBenchmark.csv"