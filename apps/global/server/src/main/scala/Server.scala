import adminAlgebra.AdminAlgebra
import auth.AuthAlgebra
import cats.effect.*
import cats.effect.std.{Random, UUIDGen}
import cats.implicits.*
import cats.syntax.all
import com.comcast.ip4s.*
import config.ConfigLoader
import config.domain.ServerConfiguration
import db.DbInitializer
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout.*
import emailAlgebra.EmailAlgebra
import org.http4s.*
import password.*
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.*
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import routes.RoutesAggregator
import routes.admin.AdminRoutes
import routes.global.GlobalRoutes
import routes.student.StudentRoutes
import routes.university.UniversityRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter
import studentAlgebra.StudentAlgebra
import token.JwtUtils
import universityAlgebra.UniversityAlgebra

trait Server[F[_]]{
  def runServer:F[Unit]
}

object Server {
  def apply[F[_]](using F: Async[F]): Server[F] = new Server[F]:
    override def runServer: F[Unit] = for {
      given Logger[F] <- Slf4jLogger.create[F]
      configs <- ConfigLoader[F].load()
      given Http4sServerInterpreter[F] <- F.delay(Http4sServerInterpreter[F]())
      given Random[F] <- Random.scalaUtilRandom[F]
      given JwtUtils[F] <- F.delay(JwtUtils[F](configs.jwt.sha256Key))
      given PasswordHasher[F] <- F.delay(PasswordHasher[F])
      given UUIDGen[F] <- F.delay(UUIDGen.fromSync[F])
      redisClient <- F.delay(Redis[F].utf8("redis://localhost"))
      mongoClient <- F.delay(DbInitializer[F](configs.db).initDb())
      _ <- (redisClient, mongoClient).tupled.use { (redis, mongo) =>
        for {
          db <- mongo.getDatabase(configs.db.database)
          emailAlgebra <- EmailAlgebra[F](configs.email)
          authAlgebra <- F.delay(AuthAlgebra[F](db, redis, configs))
          adminAlgebra <- F.delay(AdminAlgebra[F](db, emailAlgebra, configs.email))
          studentAlgebra <- F.delay(StudentAlgebra[F](db,emailAlgebra,configs.email))
          universityAlgebra <- F.delay(UniversityAlgebra[F](db))
          globalRoutes <- GlobalRoutes[F](authAlgebra).routes
          adminRoutes <- AdminRoutes[F](configs.admin, adminAlgebra).routes
          studentRoutes <- StudentRoutes[F](studentAlgebra).routes
          universityRoutes <- UniversityRoutes[F](universityAlgebra).routes
          routes <- RoutesAggregator[F](globalRoutes ++ adminRoutes ++ universityRoutes ++ studentRoutes).aggregate()
          _ <- startServer(configs.server, routes)
        } yield ()
      }
    } yield ()

    private def startServer(serverConfiguration: ServerConfiguration,routes: HttpRoutes[F]): F[Nothing] =
      EmberServerBuilder
      .default[F]
      .withHost(
        Host.fromString(serverConfiguration.host).getOrElse(host"localhost")
      )
      .withPort(Port.fromInt(serverConfiguration.port).getOrElse(port"8080"))
      .withHttpApp(routes.orNotFound)
      .build
      .useForever
}