ThisBuild / organization := "org.aksw"
ThisBuild / version := "1.0" // Release.Fix
ThisBuild / scalaVersion := "2.11.12"

lazy val sansaVersion = "0.6.0"
lazy val sparkVersion = "2.4.3"

lazy val root = (project in file(".")).settings(
  name := "shape-inferencer",
  resolvers ++= Seq(
    "AKSW Maven Releases" at "http://maven.aksw.org/archiva/repository/internal",
  ),
  libraryDependencies ++= Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
    "net.sansa-stack" %% "sansa-rdf-spark" % sansaVersion,
    "org.scala-lang" % "scala-library" % scalaVersion.value,
    "com.intel.analytics.bigdl" % "bigdl-SPARK_2.2" % "0.4.0",
    "javax.ws.rs" % "javax.ws.rs-api" % "2.1" artifacts (Artifact(
      "javax.ws.rs-api",
      "jar",
      "jar"
    ))
  ).map(_.exclude("org.slf4j", "*")),
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3"
  ),
  dependencyOverrides ++= Seq(
    "com.fasterxml.jackson.core" % "jackson-core" % "2.8.7",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.7",
    "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.7",
    "org.apache.jena" % "jena-core" % "3.11.0"
  ),
)
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "services", xs @ _*) => MergeStrategy.filterDistinctLines
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
logLevel in assembly := Level.Info
scalacOptions ++= Seq(
  "-deprecation",
// format: off
  "-encoding", "UTF-8",
// format: on
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard",
)
