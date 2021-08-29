name := "HTags"

version := "0.1"

scalaVersion := "2.12.12"
val sparkVersion = "3.0.0"
val twitter4jVersion = "4.0.4"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.0.0",
  "org.apache.spark" %% "spark-sql" % "3.0.0",
  "org.apache.spark" %% "spark-mllib" % "3.0.0",
  "org.apache.spark" %% "spark-streaming" % "3.0.0",
  "org.twitter4j" % "twitter4j-core" % "4.0.4",
  "org.twitter4j" % "twitter4j-stream" % "4.0.4"
)

libraryDependencies += "com.github.wookietreiber" %% "scala-chart" % "latest.integration"
libraryDependencies += "com.itextpdf" % "itextpdf" % "latest.integration"
libraryDependencies += "org.jfree" % "jfreesvg" % "latest.integration"


idePackagePrefix := Some("miu.bdt")
