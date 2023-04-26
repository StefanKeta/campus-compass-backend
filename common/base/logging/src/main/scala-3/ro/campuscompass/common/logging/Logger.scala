package ro.campuscompass.common.logging

import cats.*
import cats.effect.*
import cats.effect.implicits.*
import cats.effect.kernel.MonadCancel
import cats.effect.kernel.Resource.ExitCase
import cats.implicits.*
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.Slf4jFactory

class Logger[F[_]] private[logging] (logger: StructuredLogger[F]) {

  private def sourceCtx(s: Source): Map[String, String] =
    Map(
      "sourceFileName" -> s.file,
      "sourceLine" -> s.line.toString
    )

  def trace(msg: => String)(using s: Source): F[Unit] =
    trace(msg)(Map.empty)

  def trace(msg: => String, e: Throwable)(using s: Source): F[Unit] =
    trace(msg, e)(Map.empty)

  def trace(msg: => String)(ctx: Map[String, String])(using s: Source): F[Unit] =
    logger.trace(sourceCtx(s) ++ ctx)(msg)

  def trace(msg: => String, e: Throwable)(ctx: Map[String, String])(using s: Source): F[Unit] =
    logger.trace(sourceCtx(s) ++ ctx, e)(msg)

  def debug(msg: => String)(using s: Source): F[Unit] =
    debug(msg)(Map.empty)

  def debug(msg: => String, e: Throwable)(using s: Source): F[Unit] =
    debug(msg, e)(Map.empty)

  def debug(msg: => String)(ctx: Map[String, String])(using s: Source): F[Unit] =
    logger.debug(sourceCtx(s) ++ ctx)(msg)

  def debug(msg: => String, e: Throwable)(ctx: Map[String, String])(using s: Source): F[Unit] =
    logger.debug(sourceCtx(s) ++ ctx, e)(msg)

  def info(msg: => String)(using s: Source): F[Unit] =
    info(msg)(Map.empty)

  def info(msg: => String, e: Throwable)(using s: Source): F[Unit] =
    info(msg, e)(Map.empty)

  def info(msg: => String)(ctx: Map[String, String])(using s: Source): F[Unit] =
    logger.info(sourceCtx(s) ++ ctx)(msg)

  def info(msg: => String, e: Throwable)(ctx: Map[String, String])(using s: Source): F[Unit] =
    logger.info(sourceCtx(s) ++ ctx, e)(msg)

  def warn(msg: => String)(using s: Source): F[Unit] =
    warn(msg)(Map.empty)

  def warn(msg: => String, e: Throwable)(using s: Source): F[Unit] =
    warn(msg, e)(Map.empty)

  def warn(msg: => String)(ctx: Map[String, String])(using s: Source): F[Unit] =
    logger.warn(sourceCtx(s) ++ ctx)(msg)

  def warn(msg: => String, e: Throwable)(ctx: Map[String, String])(using s: Source): F[Unit] =
    logger.warn(sourceCtx(s) ++ ctx, e)(msg)

  def error(msg: => String)(using s: Source): F[Unit] =
    error(msg)(Map.empty)

  def error(msg: => String, e: Throwable)(using s: Source): F[Unit] =
    error(msg, e)(Map.empty)

  def error(msg: => String)(ctx: Map[String, String])(using s: Source): F[Unit] =
    logger.error(sourceCtx(s) ++ ctx)(msg)

  def error(msg: => String, e: Throwable)(ctx: Map[String, String])(using s: Source): F[Unit] =
    logger.error(sourceCtx(s) ++ ctx, e)(msg)

}
