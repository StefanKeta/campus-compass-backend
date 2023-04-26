package ro.campuscompass.common.effect

import cats.effect.{Clock, SyncIO}
import cats.syntax.all.*
import cats.{Functor, Monad}

import java.time.{Instant, LocalDateTime, OffsetDateTime, ZoneId}

object TimeUtils {
  def currentInstant[F[_]: Clock]: F[Instant] =
    Clock[F].realTimeInstant

  def currentLocalDateTime[F[_]: Clock: Functor]: F[LocalDateTime] =
    currentInstant.map(now => LocalDateTime.from(now))

  def currentDateTime[F[_]: Clock: Functor](zone: ZoneId = ZoneId.of("UTC")): F[OffsetDateTime] =
    currentInstant.map(now => OffsetDateTime.ofInstant(now, zone))

  def timestampedEffect[F[_]: Clock: Monad, A](f: F[A]): F[(A, Instant, Instant)] =
    for {
      startTime <- TimeUtils.currentInstant
      result    <- f
      endTime   <- TimeUtils.currentInstant
    } yield (result, startTime, endTime)
}
