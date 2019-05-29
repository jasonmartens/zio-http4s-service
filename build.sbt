name := "zendesk-http4s"

version := "0.1"

scalaVersion := "2.12.8"

val circeVersion = "0.11.1"
val http4sVersion = "0.20.1"
val slf4jVersion = "1.7.26"
val zioVersion = "1.0-RC4"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,

  "org.scalaz" %% "scalaz-zio" % zioVersion,
  "org.scalaz" %% "scalaz-zio-interop-cats" % zioVersion,

  "io.circe" %% "circe-generic" % circeVersion,
  
  "org.slf4j" % "slf4j-log4j12" % slf4jVersion,
)
