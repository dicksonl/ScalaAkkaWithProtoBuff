name := "DistributedTransform"

version := "1.0"

//fork := true

scalaVersion := "2.12.3"

mainClass in (Compile, run) := Some("Cluster.Main")

mainClass in (Compile, packageBin) := Some("Cluster.Main")

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" % "akka-protobuf_2.12" % "2.5.7",
    "com.typesafe.akka" % "akka-actor_2.12" % "2.5.7",
    "com.typesafe.akka" % "akka-slf4j_2.12" % "2.5.7",
    "com.typesafe.akka" % "akka-remote_2.12" % "2.5.7",
    "com.typesafe.akka" % "akka-cluster_2.12" % "2.5.7",
    "com.github.nscala-time" % "nscala-time_2.12" % "2.18.0",
    "com.microsoft.sqlserver" % "mssql-jdbc" % "6.2.2.jre8",
    "au.com.bytecode" % "opencsv" % "2.4",
    "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf")
}

PB.pythonExe := "C:\\Python27\\Python.exe"

PB.targets in Compile := Seq(
  scalapb.gen(flatPackage=false) -> (sourceManaged in Compile).value
)
