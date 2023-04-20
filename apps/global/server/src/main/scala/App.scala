import cats.effect.{IO, IOApp}

object App extends IOApp.Simple {
  override def run: IO[Unit] = for{
    _ <- Server[IO].runServer
  } yield ()
}
