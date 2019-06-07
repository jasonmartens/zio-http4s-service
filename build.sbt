name := "zendesk-http4s"

version := "0.1"

scalaVersion := "2.12.8"

val circeVersion = "0.11.1"
val doobieVersion = "0.7.0"
val flywayVersion = "5.2.4"
val h2Version = "1.4.199"
val http4sVersion = "0.20.1"
val pureConfigVersion = "0.11.0"
val scalatestVersion = "3.0.5"
val slf4jVersion = "1.7.26"
val zioVersion = "1.0-RC5"


libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,

  "org.scalaz" %% "scalaz-zio" % zioVersion,
  "org.scalaz" %% "scalaz-zio-interop-shared" % zioVersion,
  "org.scalaz" %% "scalaz-zio-interop-cats" % zioVersion,

  "io.circe" %% "circe-generic" % circeVersion,

  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,

  "com.h2database" % "h2" % h2Version,

  "org.flywaydb" % "flyway-core" % flywayVersion,

  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,

  "org.slf4j" % "slf4j-log4j12" % slf4jVersion,

  "org.scalactic" %% "scalactic" % scalatestVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % "test",
)

scalacOptions ++= Seq("-Ypartial-unification")
