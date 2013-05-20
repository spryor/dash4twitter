import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "dash4twitter"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    "org.twitter4j" % "twitter4j-core" % "3.0.3",
    "org.twitter4j" % "twitter4j-stream" % "3.0.3",
    "org.rogach" %% "scallop" % "0.8.1",
    "org.scalanlp" % "chalk" % "1.1.2",
    "com.cybozu.labs" % "langdetect" % "1.1-20120112",
    //"org.apache.lucene" % "lucene-core" % "4.2.0",
    //"org.apache.lucene" % "lucene-analyzers-common" % "4.2.0",
    //"org.apache.lucene" % "lucene-queryparser" % "4.2.0"
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
