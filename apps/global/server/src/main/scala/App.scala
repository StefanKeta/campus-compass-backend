import adminAlgebra.AdminAlgebra
import cats.effect.*
import cats.implicits.*
import com.comcast.ip4s.*
import config.ConfigLoader
import config.domain.ServerConfiguration
import db.DbInitializer
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.*
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import routes.RoutesAggregator
import routes.admin.AdminRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter

object App extends IOApp.Simple {

  private def runServer[F[_]:Async](
      serverConfiguration: ServerConfiguration,
      routes: HttpRoutes[F]
  ): Resource[F, Server] = EmberServerBuilder
    .default[F]
    .withHost(
      Host.fromString(serverConfiguration.host).getOrElse(host"localhost")
    )
    .withPort(Port.fromInt(serverConfiguration.port).getOrElse(port"8080"))
    .withHttpApp(routes.orNotFound)
    .build

  override def run: IO[Unit] = for {
    given Logger[IO] <- Slf4jLogger.create[IO]
    given Http4sServerInterpreter[IO] <- IO(Http4sServerInterpreter[IO]())
    configs <- ConfigLoader[IO].load()
    _ <- DbInitializer[IO](configs.db).initDb().use { client =>
      for {
        db <- client.getDatabase(configs.db.database)
        adminAlgebra <- IO(AdminAlgebra[IO](db))
        adminRoutes <- IO(AdminRoutes[IO](configs.admin, adminAlgebra))
        routes <- RoutesAggregator[IO](adminRoutes).aggregate()
        _ <- runServer(configs.server, routes).useForever
      } yield ()
    }
  }yield ()
}
