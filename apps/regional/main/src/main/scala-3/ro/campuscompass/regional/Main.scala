package ro.campuscompass.regional

import cats.effect.unsafe.IORuntimeConfig
import cats.effect.{ ExitCode, IO, IOApp }
import ro.campuscompass.common.logging.Logging

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.Duration
import scala.util.Try

object Main extends IOApp with Logging {

  override def runtimeConfig: IORuntimeConfig = {
    val DEFAULT_SHUTDOWN_HOOK_TIMEOUT = 30.seconds

    IORuntimeConfig().copy(
      shutdownHookTimeout =
        Option(System.getenv("SHUTDOWN_HOOK_TIMEOUT"))
          .filterNot(_.isEmpty)
          .flatMap { str =>
            Try(Duration(str))
              .orElse(Try(Duration(str.toLong, TimeUnit.SECONDS)))
              .toOption
          }
          .getOrElse(DEFAULT_SHUTDOWN_HOOK_TIMEOUT)
    )
  }

  override def reportFailure(err: Throwable): IO[Unit] =
    logger[IO].error("Uncaught error in thread-pool", err).as(())

  override def run(args: List[String]): IO[ExitCode] =
    RegionalApp[IO].useForever.as(ExitCode.Success)
}
