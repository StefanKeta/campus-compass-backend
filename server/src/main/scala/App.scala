import cats.effect.*
import cats.implicits.*
import com.comcast.ip4s.*
import config.ConfigLoader
import config.domain.ServerConfiguration
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.*
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import routes.Routes

object App extends IOApp.Simple {

  private def runServer(
      serverConfiguration: ServerConfiguration,
      routes: Routes[IO]
  ): Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(
      Host.fromString(serverConfiguration.host).getOrElse(host"localhost")
    )
    .withPort(Port.fromInt(serverConfiguration.port).getOrElse(port"8080"))
    .withHttpApp(routes.helloRoute.orNotFound)
    .build

  override def run: IO[Unit] = for {
    given Logger[IO] <- Slf4jLogger.create[IO]
    configs <- ConfigLoader[IO].load()
    _ <- runServer(configs.server, new Routes[IO]).useForever
  } yield ()
}
