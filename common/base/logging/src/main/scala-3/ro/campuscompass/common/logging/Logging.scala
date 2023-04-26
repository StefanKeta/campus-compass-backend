package ro.campuscompass.common.logging

import cats.effect.Sync
import org.typelevel.log4cats.slf4j.*
import org.typelevel.log4cats.{LoggerFactory, LoggerName}

trait Logging {
  def logger[F[_]: Sync]: Logger[F] =
    new Logger(Slf4jFactory[F].getLoggerFromClass(getClass))
}
