package ro.campuscompass.common.time

import cats.effect.kernel.Sync

import java.time.Instant

object Time {
  def now[F[_]:Sync]: F[Instant] = Sync[F].delay(Instant.now)
}
