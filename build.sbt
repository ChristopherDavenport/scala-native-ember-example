ThisBuild / scalaVersion     := "3.2.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "io.chrisdavenport"
ThisBuild / organizationName := "Christopher Davenport"

enablePlugins(ScalaNativePlugin)

name := "Scala Native Ember Example"

libraryDependencies ++= Seq(
  "com.armanbilge" %%% "epollcat" % "0.1.1", // Runtime
  "org.http4s" %%% "http4s-ember-client" % "0.23.16",
  "org.http4s" %%% "http4s-ember-server" % "0.23.16",
  "org.http4s" %%% "http4s-dsl" % "0.23.16",
  "org.http4s" %%% "http4s-circe" % "0.23.16",
)

val isLinux = Option(System.getProperty("os.name")).exists(_.toLowerCase().contains("linux"))
val isMacOs = Option(System.getProperty("os.name")).exists(_.toLowerCase().contains("mac"))
val isArm = Option(System.getProperty("os.arch")).exists(_.toLowerCase().contains("aarch64"))

nativeConfig ~= { c =>
  if (isLinux) { // brew-installed s2n
    c.withLinkingOptions(c.linkingOptions :+ "-L/home/linuxbrew/.linuxbrew/lib")
  } else if (isMacOs) // brew-installed OpenSSL
    if(isArm) c.withLinkingOptions(c.linkingOptions :+ "-L/opt/homebrew/opt/openssl@3/lib")
    else c.withLinkingOptions(c.linkingOptions :+ "-L/usr/local/opt/openssl@3/lib")
  else c
}
envVars ++= {
  val ldLibPath =
    if (isLinux)
      Map("LD_LIBRARY_PATH" -> "/home/linuxbrew/.linuxbrew/lib")
    else Map("LD_LIBRARY_PATH" -> "/usr/local/opt/openssl@1.1/lib")
  Map("S2N_DONT_MLOCK" -> "1") ++ ldLibPath
}
