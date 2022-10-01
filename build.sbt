ThisBuild / scalaVersion     := "3.2.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "io.chrisdavenport"
ThisBuild / organizationName := "Christopher Davenport"

enablePlugins(ScalaNativePlugin)
enablePlugins(ScalaNativeBrewedConfigPlugin)

name := "Scala Native Ember Example"

libraryDependencies ++= Seq(
  "com.armanbilge" %%% "epollcat" % "0.1.1", // Runtime
  "org.http4s" %%% "http4s-ember-client" % "0.23.16",
  "org.http4s" %%% "http4s-ember-server" % "0.23.16",
  "org.http4s" %%% "http4s-dsl" % "0.23.16",
  "org.http4s" %%% "http4s-circe" % "0.23.16",
)

nativeBrewFormulas += "s2n"
envVars ++= Map("S2N_DONT_MLOCK" -> "1")
