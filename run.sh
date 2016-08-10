#!/usr/bin/env bash
export MESOS_NATIVE_JAVA_LIBRARY=""
export SPARK_EXECUTOR_URI=""
sbt clean
sbt "jmh:run -rf csv -rff results.csv"