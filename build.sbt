ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

val catsV = "2.9.0"
val catsEffectV = "3.4.8"
val circeV = "0.14.5"
val http4sV = "0.23.18"
val javaxMailV = "1.6.2"
val log4catsV = "2.5.0"
val logbackV = "1.4.6"
val mongo4catsV = "0.6.10"
val pureconfigV = "0.17.2"
val redis4catsV = "1.4.1"
val tapirV = "1.2.10"
val tsecV = "0.4.0"

lazy val baseDependencies = Seq(
  "org.typelevel" %% "cats-effect" % catsEffectV,
  "org.typelevel" %% "cats-core" % catsV,
  "org.typelevel" %% "log4cats-slf4j" % log4catsV,
  "ch.qos.logback" % "logback-classic" % logbackV,
  "dev.profunktor" %% "redis4cats-effects" % redis4catsV,
  "io.github.kirill5k" %% "mongo4cats-core" % mongo4catsV,
  "io.github.kirill5k" %% "mongo4cats-circe" % mongo4catsV,
)

lazy val root = (project in file("."))
  .settings(
    name := "backend"
  )
  .aggregate(config, globalDomain, globalEndpoints, globalRoutes, globalServer)

lazy val config = (project in file(
  "./common/config"
)).settings(
  name := "config",
  libraryDependencies ++= baseDependencies ++ Seq(
    "com.github.pureconfig" %% "pureconfig-core" % pureconfigV,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % pureconfigV
  )
)

lazy val crypto = (project in file(
  "./common/crypto"
))
  .settings(
    name := "crypto",
    libraryDependencies ++= baseDependencies ++ Seq(
      "io.github.jmcardon" %% "tsec-common" % tsecV,
      "io.github.jmcardon" %% "tsec-password" % tsecV,
      "io.github.jmcardon" %% "tsec-jwt-mac" % tsecV,
    )
  )

lazy val email = (project in file("./common/email"))
  .settings(
    name := "email",
    libraryDependencies ++= baseDependencies ++ Seq(
      "com.sun.mail" % "javax.mail" % javaxMailV withSources ()
    )
  )
  .dependsOn(config)

lazy val globalAdminAlgebra = (project in file("apps/global/algebra/admin"))
  .settings(
    name := "admin",
    libraryDependencies ++= baseDependencies
  )
  .dependsOn(config, crypto, email, globalDomain, globalStorage)

lazy val globalAuthAlgebra = (project in file("./apps/global/algebra/auth"))
  .settings(
    name := "authentication",
    libraryDependencies ++= baseDependencies
  )
  .dependsOn(config, crypto, globalStorage)

lazy val globalDomain = (project in file("./apps/global/domain"))
  .settings(
    name := "domain",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirV
    )
  )

lazy val globalEndpoints = (project in file("./apps/global/endpoints"))
  .settings(
    name := "endpoints",
    libraryDependencies ++= baseDependencies ++ Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirV,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirV
    )
  )
  .dependsOn(globalDomain, globalStorage)

lazy val globalRoutes = (project in file("./apps/global/routes"))
  .settings(
    name := "routes",
    libraryDependencies ++= baseDependencies ++ Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirV,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirV,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirV,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirV
    )
  )
  .dependsOn(
    config,
    globalAdminAlgebra,
    globalAuthAlgebra,
    globalDomain,
    globalEndpoints,
    globalStorage,
    globalStudentAlgebra,
    globalUniversityAlgebra,
  )

lazy val globalServer = (project in file("./apps/global/server"))
  .settings(
    name := "server",
    libraryDependencies ++= baseDependencies ++ Seq(
      "org.http4s" %% "http4s-ember-server" % http4sV,
      "org.http4s" %% "http4s-dsl" % http4sV
    )
  )
  .dependsOn(
    config,
    crypto,
    email,
    globalRoutes,
    globalAdminAlgebra,
    globalAuthAlgebra
  )

lazy val globalStorage = (project in file("./apps/global/storage"))
  .settings(
    name := "storage",
    libraryDependencies ++= baseDependencies
  )
  .dependsOn(globalDomain)

lazy val globalStudentAlgebra = (project in file("./apps/global/algebra/student"))
  .settings(
    name := "student",
    libraryDependencies ++= baseDependencies
  )
  .dependsOn(config,crypto,email,globalDomain,globalStorage)

lazy val globalUniversityAlgebra = (project in file("./apps/global/algebra/university"))
  .settings(
    name := "university",
    libraryDependencies ++= baseDependencies
  )
  .dependsOn(config,crypto,globalDomain,globalStorage)
