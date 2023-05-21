import sbt.Project.projectToRef

lazy val root = Project(id = "campus-compass-backend", base = file("."))
  .aggregate(allProjects.map(projectToRef) *)
  .settings(sharedSettings)
  .settings(
    name := "campus-compass-backend",
    Compile / compile := (Compile / compile)
      .dependsOn(Compile / scalafmtSbt)
      .value
  )

lazy val allProjects = List(
  commonBase,
  commonBaseCrypto,
  commonBaseEffect,
  commonBaseHttp,
  commonBaseLogging,
  commonBaseType,
  commonBaseTime,
  commonFirebase,
  commonMongo,
  commonRedis,
  commonEmail,
  commonDomain,
  commonMinio,
  // GLOBAL
  globalMain,
  globalHttpServer,
  globalDomain,
  globalAuthAlgebra,
  globalAdminAlgebra,
  globalStudentAlgebra,
  globalUniversityAlgebra,
  globalPersistence,
  globalClient,
  // REGIONAL,
  regionalMain,
  regionalHttpServer,
  regionalDomain,
  regionalAuthorizationAlgebra,
  regionalUniversityAlgebra,
  regionalApplicationAlgebra,
  regionalPersistence,
)

def campusCompassModule(path: List[String], baseDir: Option[String] = None): Project = {
  val id   = path.map(_.replaceAll("[^\\w-]+", "")).mkString("-")
  val base = path.foldLeft(file(baseDir.getOrElse("."))) { (dir, file) => dir / file }

  Project(id = id, base = base)
    .settings(sharedSettings)
    .settings(
      initialize := {
        val _ = initialize.value
        if (!allProjects.exists(_.id == id))
          sys.error(s"Sub-project `$id` was not declared in `allProjects`")
      }
    )
}

lazy val sharedSettings = Seq(
  ThisBuild / version := "latest",
  ThisBuild / organization := "ro.campus-compass",
  ThisBuild / scalaVersion := Dependencies.projectScalaVersion,
  scalacOptions ++= Seq(
    "-Xmax-inlines:33"
  ),
  Compile / compile / wartremoverErrors :=
    Seq(
      Wart.ExplicitImplicitTypes,
      Wart.FinalCaseClass,
      Wart.FinalVal,
      Wart.IsInstanceOf,
      Wart.LeakingSealed,
      Wart.NonUnitStatements,
      Wart.Null,
      Wart.OptionPartial,
      Wart.Return,
      Wart.Serializable,
      Wart.StringPlusAny,
      Wart.ToString
    ),
  libraryDependencies ++= Seq(
    Dependencies.cats,
    Dependencies.catsEffect,
    Dependencies.catsRetry,
    Dependencies.circe,
    Dependencies.fs2,
    Dependencies.fs2IO,
    Dependencies.pureconfig,
    Dependencies.pureconfigCatsEffect
  )
)

// * ------------------------------------------------------- *
// * ------------------------------------------------------- *
//     Common Base Modules
// * ------------------------------------------------------- *
// * ------------------------------------------------------- *

def commonBaseModule(path: String*): Project =
  campusCompassModule("common" :: "base" :: path.toList)

// aggregates all common base modules
lazy val commonBase =
  commonBaseModule()
    .dependsOn(
      commonBaseCrypto,
      commonBaseEffect,
      commonBaseLogging,
      commonBaseType,
      commonBaseHttp,
      commonBaseTime
    )
    .aggregate(
      commonBaseCrypto,
      commonBaseEffect,
      commonBaseLogging,
      commonBaseType,
      commonBaseHttp,
      commonBaseTime
    )

lazy val commonBaseCrypto =
  commonBaseModule("crypto")
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.tsecCommon,
        Dependencies.tsecPassword,
        Dependencies.tsecJwtMac,
      )
    )
    .dependsOn(commonBaseType)

lazy val commonBaseEffect =
  commonBaseModule("effect")
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.apacheCommonsText
      )
    )
    .dependsOn(commonBaseLogging)

lazy val commonBaseLogging =
  commonBaseModule("logging")
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.logback,
        Dependencies.log4cats,
        Dependencies.sourcecode
      )
    )

lazy val commonBaseType =
  commonBaseModule("type")

lazy val commonBaseHttp =
  commonBaseModule("http")
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.tapir,
        Dependencies.tapirCirce,
        Dependencies.tapirHttp4s,
        Dependencies.tapirSwagger,
        Dependencies.http4s,
        Dependencies.http4sEmber,
        Dependencies.http4sClient,
        Dependencies.http4sCirce
      )
    )

lazy val commonBaseTime =
  commonBaseModule("time")

// * ------------------------------------------------------- *
// * ------------------------------------------------------- *
//     Common Modules
// * ------------------------------------------------------- *
// * ------------------------------------------------------- *

def commonModule(path: String*): Project =
  campusCompassModule("common" :: path.toList)
    .dependsOn(commonBase)

lazy val commonFirebase =
  commonModule("firebase")
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.firebase
      )
    )

lazy val commonMongo =
  commonModule("mongo")
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.mongo4cats,
        Dependencies.mongo4catsCirce
      )
    )

lazy val commonRedis =
  commonModule("redis")
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.redis4cats
      )
    )

lazy val commonEmail =
  commonModule("email")
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.javaxMail
      )
    )

lazy val commonMinio =
  commonModule("minio")
    .settings(
      libraryDependencies ++= Seq(
        Dependencies.minio
      )
    )

lazy val commonDomain =
  commonModule("domain")

// * ------------------------------------------------------- *
// * ------------------------------------------------------- *
//     Global Server Modules
// * ------------------------------------------------------- *
// * ------------------------------------------------------- *
def globalModule(path: String*): Project =
  campusCompassModule("global" :: path.toList, Some("apps"))
    .dependsOn(commonBase)

lazy val globalMain =
  globalModule("main")
    .enablePlugins(JavaAppPackaging)
    .enablePlugins(DockerPlugin)
    .settings(
      dockerExposedPorts := Seq(8080),
      dockerBaseImage := "openjdk:17"
    )
    .dependsOn(commonRedis)
    .dependsOn(globalHttpServer)
    .dependsOn(globalPersistence)
    .dependsOn(globalClient)

lazy val globalHttpServer =
  globalModule("http-server")
    .dependsOn(commonBaseHttp)
    .dependsOn(globalDomain)
    .dependsOn(globalAdminAlgebra)
    .dependsOn(globalAuthAlgebra)
    .dependsOn(globalStudentAlgebra)
    .dependsOn(globalUniversityAlgebra)
    .dependsOn(globalClient)

lazy val globalDomain =
  globalModule("domain")
    .dependsOn(commonDomain)

def globalAlgebraModule(path: String*): Project =
  globalModule(("algebra" :: path.toList) *)
    .dependsOn(globalDomain)
    .dependsOn(globalPersistence)
    .dependsOn(globalClient)

lazy val globalAdminAlgebra =
  globalAlgebraModule("admin")
    .dependsOn(commonEmail)

lazy val globalAuthAlgebra =
  globalAlgebraModule("auth")
    .dependsOn(commonRedis)

lazy val globalStudentAlgebra =
  globalAlgebraModule("student")
    .dependsOn(commonEmail, globalClient)

lazy val globalUniversityAlgebra =
  globalAlgebraModule("university")

lazy val globalPersistence =
  globalModule("persistence")
    .dependsOn(commonMongo)
    .dependsOn(commonFirebase)
    .dependsOn(globalDomain)

lazy val globalClient =
  globalModule("client")
    .dependsOn(globalDomain)

// * ------------------------------------------------------- *
// * ------------------------------------------------------- *
//     Regional Server Modules
// * ------------------------------------------------------- *
// * ------------------------------------------------------- *
def regionalModule(path: String*): Project =
  campusCompassModule("regional" :: path.toList, Some("apps"))
    .dependsOn(commonBase)

lazy val regionalMain =
  regionalModule("main")
    .enablePlugins(JavaAppPackaging)
    .enablePlugins(DockerPlugin)
    .settings(
      dockerExposedPorts := Seq(8080),
      dockerBaseImage := "openjdk:17"
    )
    .dependsOn(regionalHttpServer)
    .dependsOn(regionalPersistence)

lazy val regionalHttpServer =
  regionalModule("http-server")
    .dependsOn(commonBaseHttp)
    .dependsOn(regionalDomain)
    .dependsOn(regionalAuthorizationAlgebra)
    .dependsOn(regionalUniversityAlgebra)
    .dependsOn(regionalApplicationAlgebra)

lazy val regionalDomain =
  regionalModule("domain")
    .dependsOn(commonDomain)

def regionalAlgebraModule(path: String*): Project =
  regionalModule(("algebra" :: path.toList) *)
    .dependsOn(regionalDomain)
    .dependsOn(regionalPersistence)

lazy val regionalAuthorizationAlgebra =
  regionalAlgebraModule("authorization")
    .dependsOn(commonRedis)

lazy val regionalUniversityAlgebra =
  regionalAlgebraModule("university")
    .dependsOn(commonMongo)
    .dependsOn(commonEmail)

lazy val regionalApplicationAlgebra =
  regionalAlgebraModule("application")
    .dependsOn(commonMongo)
    .dependsOn(commonMinio)

lazy val regionalPersistence =
  regionalModule("persistence")
    .dependsOn(commonMongo)
    .dependsOn(regionalDomain)
