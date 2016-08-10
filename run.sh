#!/usr/bin/env bash
sbt clean
sbt "jmh:run -rf csv -rff results.csv"