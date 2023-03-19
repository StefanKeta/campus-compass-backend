ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

val catsV = "2.9.0"
val catsEffectV = "3.4.8"
val circeV = "0.14.5"
val http4sVersion = "0.23.18"
val tapirV = "1.2.10"

lazy val dependencies = Seq(
  "org.typelevel" %% "cats-effect" % catsEffectV,
  "org.typelevel" %% "cats-core" % catsV
)

lazy val root = (project in file("."))
  .settings(
    name := "backend"
  )
  .aggregate(routes, server)

lazy val routes = (project in file("./routes"))
  .settings(
    name := "routes",
    libraryDependencies ++= dependencies ++ Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirV,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirV,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirV
    )
  )

lazy val server = (project in file("./server"))
  .settings(
    name := "server",
    libraryDependencies ++= dependencies ++ Seq(
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion
    )
  )
  .dependsOn(routes)
