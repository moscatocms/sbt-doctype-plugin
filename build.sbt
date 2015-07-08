sbtPlugin := true

organization := "org.moscatocms"

name := "sbt-doctype-plugin"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.liquibase" % "liquibase-core" % "3.4.0"
)