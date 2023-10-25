import scala.collection.mutable.ListBuffer

ThisBuild / scalaVersion     := "3.3.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "io.chrisdavenport"
ThisBuild / organizationName := "Christopher Davenport"

enablePlugins(ScalaNativePlugin)

name := "Scala Native Ember Example"

libraryDependencies ++= Seq(
  "com.armanbilge" %%% "epollcat" % "0.1.6", // Runtime
  "org.http4s" %%% "http4s-ember-client" % "0.23.23",
  "org.http4s" %%% "http4s-ember-server" % "0.23.23",
  "org.http4s" %%% "http4s-dsl" % "0.23.23",
  "org.http4s" %%% "http4s-circe" % "0.23.23",
)

val isLinux = Option(System.getProperty("os.name")).exists(_.toLowerCase().contains("linux"))
val isMacOs = Option(System.getProperty("os.name")).exists(_.toLowerCase().contains("mac"))
val isArm = Option(System.getProperty("os.arch")).exists(_.toLowerCase().contains("aarch64"))
val s2nLibPath = sys.env.get("S2N_LIBRARY_PATH")



nativeConfig ~= { c =>
  val linkOpts = ListBuffer.empty[String]
  if (isLinux) // brew-installed s2n
    linkOpts.append("-L/home/linuxbrew/.linuxbrew/lib")
  else if (isMacOs) // brew-installed OpenSSL
    if(isArm) linkOpts.append("-L/opt/homebrew/opt/openssl@3/lib")
    else linkOpts.append("-L/usr/local/opt/openssl@3/lib")
  s2nLibPath match {
    case None =>
    case Some(path) => linkOpts.append(s"-L$path")
  }
  c.withLinkingOptions(c.linkingOptions ++ linkOpts.toSeq)
}

envVars ++= {
  val ldLibPath =
    if (isLinux)
      Map("LD_LIBRARY_PATH" -> "/home/linuxbrew/.linuxbrew/lib")
    else Map("LD_LIBRARY_PATH" -> "/usr/local/opt/openssl@1.1/lib")
  Map("S2N_DONT_MLOCK" -> "1") ++ ldLibPath
}
