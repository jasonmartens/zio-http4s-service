name := "zendesk-http4s"

version := "0.1"

scalaVersion := "2.13.1"

val catsVersion = "2.0.0"
val circeVersion = "0.12.2"
val doobieVersion = "0.8.4"
val flywayVersion = "5.2.4"
val h2Version = "1.4.199"
val http4sVersion = "0.21.0-M5"
val pureConfigVersion = "0.12.1"
val scalatestVersion = "3.0.8"
val slf4jVersion = "1.7.26"
val zioVersion = "1.0.0-RC15"
val zioInteropVersion = "2.0.0.0-RC6"


libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,

  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,

  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-interop-cats" % zioInteropVersion,
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
