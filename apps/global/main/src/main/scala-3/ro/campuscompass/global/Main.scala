package ro.campuscompass.global

import cats.effect.*
import cats.effect.implicits.*
import cats.effect.unsafe.IORuntimeConfig
import cats.implicits.*
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigConvert
import ro.campuscompass.common.crypto.JwtConfig
import ro.campuscompass.common.email.EmailConfig
import ro.campuscompass.common.http.ServerConfig
import ro.campuscompass.common.logging.{Logger, Logging}
import ro.campuscompass.common.mongo.MongoDBConfig
import ro.campuscompass.common.redis.RedisConfig
import ro.campuscompass.global.algebra.admin.AdminConfig

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*
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
    GlobalApp[IO].useForever.as(ExitCode.Success)
}
