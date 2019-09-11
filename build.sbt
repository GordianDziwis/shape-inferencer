ThisBuild / organization := "org.aksw"
ThisBuild / version := "1.0" // Release.Fix
ThisBuild / scalaVersion := "2.11.12"

lazy val sansaVersion = "0.6.0"
lazy val sparkVersion = "2.4.3"

lazy val root = (project in file(".")).settings(
  name := "shape-inferencer",
  dependencyOverrides ++= Seq(
    "com.fasterxml.jackson.core" % "jackson-core" % "2.8.7",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.7",
    "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.7"
  ),
  libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
    "org.scala-lang" % "scala-library" % scalaVersion.value,
    "com.intel.analytics.bigdl" % "bigdl-SPARK_2.2" % "0.4.0",
    "javax.ws.rs" % "javax.ws.rs-api" % "2.1" artifacts (Artifact(
      "javax.ws.rs-api",
      "jar",
      "jar"
    ))
  ),
  resolvers ++= Seq(
    "AKSW Maven Releases" at "http://maven.aksw.org/archiva/repository/internal",
    "AKSW Maven Snapshots" at "http://maven.aksw.org/archiva/repository/snapshots",
    "oss-sonatype" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "Apache repository (snapshots)" at "https://repository.apache.org/content/repositories/snapshots/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "NetBeans" at "http://bits.netbeans.org/nexus/content/groups/netbeans/",
    "gephi" at "https://raw.github.com/gephi/gephi/mvn-thirdparty-repo/",
    Resolver.defaultLocal,
    Resolver.mavenLocal,
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "Apache Staging" at "https://repository.apache.org/content/repositories/staging/"
  ),
  libraryDependencies ++= Seq(
    "net.sansa-stack" %% "sansa-rdf-spark" % sansaVersion,
    "net.sansa-stack" %% "sansa-owl-spark" % sansaVersion,
    "net.sansa-stack" %% "sansa-inference-spark" % sansaVersion,
    "net.sansa-stack" %% "sansa-query-spark" % sansaVersion,
    "net.sansa-stack" %% "sansa-ml-spark" % sansaVersion
  ),
  bloopAggregateSourceDependencies in Global := true,
  bloopExportJarClassifiers in Global := Some(Set("sources"))
)

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

