import sbt.*

object Dependencies {
  lazy val projectScalaVersion = "3.2.2"

  lazy val cats = ("org.typelevel" %% "cats-core" % "2.9.0").withSources()

  lazy val catsEffect = ("org.typelevel" %% "cats-effect" % "3.4.8").withSources()

  lazy val catsRetry = ("com.github.cb372" %% "alleycats-retry" % "3.1.0").withSources()

  private lazy val fs2Version = "3.6.1"
  lazy val fs2                = ("co.fs2" %% "fs2-core" % fs2Version).withSources()
  lazy val fs2IO              = ("co.fs2" %% "fs2-io"   % fs2Version).withSources()

  private lazy val circeVersion = "0.14.5"
  lazy val circe                = ("io.circe" %% "circe-core"    % circeVersion).withSources()
  lazy val circeGeneric         = ("io.circe" %% "circe-generic" % circeVersion).withSources()
  lazy val circeParser          = ("io.circe" %% "circe-parser"  % circeVersion).withSources()

  private lazy val pureconfigVersion = "0.17.2"
  lazy val pureconfig                = ("com.github.pureconfig" %% "pureconfig-core"        % pureconfigVersion).withSources()
  lazy val pureconfigCatsEffect      = ("com.github.pureconfig" %% "pureconfig-cats-effect" % pureconfigVersion).withSources()

  private lazy val http4sVersion = "0.23.18"
  lazy val http4s                = ("org.http4s" %% "http4s-ember-server" % http4sVersion).withSources()
  lazy val http4sClient          = ("org.http4s" %% "http4s-ember-client" % http4sVersion).withSources()
  lazy val http4sEmber           = ("org.http4s" %% "http4s-dsl"          % http4sVersion).withSources()
  lazy val http4sCirce           = ("org.http4s" %% "http4s-circe"        % http4sVersion).withSources()

  private lazy val tapirVersion = "1.2.10"
  lazy val tapir                = ("com.softwaremill.sttp.tapir" %% "tapir-core"              % tapirVersion).withSources()
  lazy val tapirHttp4s          = ("com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % tapirVersion).withSources()
  lazy val tapirCirce           = ("com.softwaremill.sttp.tapir" %% "tapir-json-circe"        % tapirVersion).withSources()
  lazy val tapirSwagger         = ("com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion).withSources()

  private lazy val mongo4catsVersion = "0.6.10"
  lazy val mongo4cats                = ("io.github.kirill5k" %% "mongo4cats-core"  % mongo4catsVersion).withSources()
  lazy val mongo4catsCirce           = ("io.github.kirill5k" %% "mongo4cats-circe" % mongo4catsVersion).withSources()

  lazy val redis4cats = ("dev.profunktor" %% "redis4cats-effects" % "1.4.1").withSources()

  lazy val logback = ("ch.qos.logback" % "logback-classic" % "1.4.6").withSources()

  lazy val log4cats = ("org.typelevel" %% "log4cats-slf4j" % "2.5.0").withSources()

  private lazy val tsecVersion = "0.4.0"
  lazy val tsecCommon          = ("io.github.jmcardon" %% "tsec-common"   % tsecVersion).withSources()
  lazy val tsecPassword        = ("io.github.jmcardon" %% "tsec-password" % tsecVersion).withSources()
  lazy val tsecJwtMac          = ("io.github.jmcardon" %% "tsec-jwt-mac"  % tsecVersion).withSources()

  lazy val javaxMail = ("com.sun.mail" % "javax.mail" % "1.6.2").withSources()

  lazy val sourcecode = ("com.lihaoyi" %% "sourcecode" % "0.3.0").withSources()

  lazy val apacheCommonsText = ("org.apache.commons" % "commons-text" % "1.10.0").withSources()

  lazy val minio = ("io.minio" % "minio" % "8.5.2").withSources()

  lazy val firebase = ("com.google.cloud" % "google-cloud-firestore" % "3.9.1").withSources()
}
