ThisBuild / scalaVersion     := "3.2.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "io.chrisdavenport"
ThisBuild / organizationName := "Christopher Davenport"

enablePlugins(ScalaNativePlugin)

name := "Scala Native Ember Example"

libraryDependencies ++= Seq(
  "com.armanbilge" %%% "epollcat" % "0.1.0", // Runtime
  "org.http4s" %%% "http4s-ember-client" % "0.23.16",
  "org.http4s" %%% "http4s-ember-server" % "0.23.16",
  "org.http4s" %%% "http4s-dsl" % "0.23.16",
  "org.http4s" %%% "http4s-circe" % "0.23.16",
)

val isLinux = {
  val osName = Option(System.getProperty("os.name"))
  osName.exists(_.toLowerCase().contains("linux"))
}
val isMacOs = {
  val osName = Option(System.getProperty("os.name"))
  osName.exists(_.toLowerCase().contains("mac"))
}

nativeConfig ~= { c =>
  if (isLinux) { // brew-installed s2n
    c.withLinkingOptions(c.linkingOptions :+ "-L/home/linuxbrew/.linuxbrew/lib")
  } else if (isMacOs) // brew-installed OpenSSL
    c.withLinkingOptions(c.linkingOptions :+ "-L/usr/local/opt/openssl@1.1/lib")
  else c
}
Test / envVars ++= {
  val ldLibPath =
    if (isLinux)
      Map("LD_LIBRARY_PATH" -> "/home/linuxbrew/.linuxbrew/lib")
    else Map("LD_LIBRARY_PATH" -> "/usr/local/opt/openssl@1.1/lib")
  Map("S2N_DONT_MLOCK" -> "1") ++ ldLibPath
}
