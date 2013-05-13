name := "dash4twitter"

version := "0.2.0"

organization := "edu.utexas"

scalaVersion := "2.10.1"

crossPaths := false

retrieveManaged := true

libraryDependencies ++= Seq(
  "org.rogach" %% "scallop" % "0.8.1",
  "org.scalanlp" % "chalk" % "1.1.2"
)

