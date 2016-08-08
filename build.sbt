name := "SciSparkPerf"

version := "1.0"

scalaVersion := "2.10.6"

enablePlugins(JmhPlugin)

libraryDependencies ++= Seq(
  "scisparktestexperiments" % "scisparktestexperiments_2.10" % "1.0"
)
