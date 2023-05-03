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
  commonMongo,
  commonRedis,
  commonEmail,
  commonDomain,
  globalMain,
  globalHttpServer,
  globalDomain,
  globalAuthAlgebra,
  globalAdminAlgebra,
  globalStudentAlgebra,
  globalUniversityAlgebra,
  globalPersistence
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
  ThisBuild / version := "1.0.0",
  ThisBuild / organization := "ro.campus-compass",
  ThisBuild / scalaVersion := Dependencies.projectScalaVersion,
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
        Dependencies.http4sEmber
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
    .dependsOn(commonRedis)
    .dependsOn(globalHttpServer)
    .dependsOn(globalPersistence)

lazy val globalHttpServer =
  globalModule("http-server")
    .dependsOn(commonBaseHttp)
    .dependsOn(globalDomain)
    .dependsOn(globalAdminAlgebra)
    .dependsOn(globalAuthAlgebra)
    .dependsOn(globalStudentAlgebra)
    .dependsOn(globalUniversityAlgebra)

lazy val globalDomain =
  globalModule("domain")
    .dependsOn(commonDomain)

def globalAlgebraModule(path: String*): Project =
  globalModule(("algebra" :: path.toList) *)
    .dependsOn(globalDomain)
    .dependsOn(globalPersistence)

lazy val globalAdminAlgebra =
  globalAlgebraModule("admin")
    .dependsOn(commonEmail)

lazy val globalAuthAlgebra =
  globalAlgebraModule("auth")
    .dependsOn(commonRedis)

lazy val globalStudentAlgebra =
  globalAlgebraModule("student")
    .dependsOn(commonEmail)

lazy val globalUniversityAlgebra =
  globalAlgebraModule("university")

lazy val globalPersistence =
  globalModule("persistence")
    .dependsOn(commonMongo)
    .dependsOn(globalDomain)

addCommandAlias("runGlobal", "global-main/run")
addCommandAlias("runRegional", "regional-main/run")
