sbtPlugin := true

organization := "org.moscatocms"

name := "sbt-doctype-plugin"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.reflections" % "reflections" % "0.9.10",
  "com.h2database" % "h2" % "1.4.187"
)

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

addSbtPlugin("org.moscatocms" %% "liquibase-utils" % "1.0.0-SNAPSHOT")