val http4sVersion = "0.14.1"

name := "RogerRoger"

description := "A simple api for getting metrics"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "rho-core" % "0.11.0",
  "org.http4s" %% "rho-swagger" % "0.11.0",
  "io.argonaut" %% "argonaut" % "6.1",
  "org.scalaz.stream" %% "scalaz-stream" % "0.8",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "com.github.dblock" % "oshi-core" % "2.6.1"
)
    