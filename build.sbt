ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

lazy val dependencies = libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.4.8",
  "org.typelevel" %% "cats-core" % "2.9.0",
  "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.2.10"
)

lazy val root = (project in file("."))
  .settings(
    name := "backend"
  )

lazy val server = (project in file("./server"))
  .settings(
    name := "server",
    dependencies
  )
