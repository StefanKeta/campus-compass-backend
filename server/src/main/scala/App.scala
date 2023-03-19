import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.*
import routes.Routes

object App extends IOApp.Simple {

  private def runServer(routes: Routes[IO]): Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(host"localhost")
    .withPort(port"8080")
    .withHttpApp(routes.helloRoute.orNotFound)
    .build

  override def run: IO[Unit] = for {
    _ <- runServer(new Routes[IO]).useForever
  } yield ()
}
