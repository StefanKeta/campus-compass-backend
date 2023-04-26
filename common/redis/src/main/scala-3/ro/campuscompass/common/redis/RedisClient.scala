package ro.campuscompass.common.redis

import cats.effect.*
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import ro.campuscompass.common.logging.Logging

object RedisClient extends Logging {
  def apply[F[_]: Async](
    config: RedisConfig
  ): Resource[F, RedisCommands[F, String, String]] = {
    import dev.profunktor.redis4cats.effect.Log.NoOp.*
    import dev.profunktor.redis4cats.effect.MkRedis.forAsync

    Redis[F].utf8(config.redisUri)
  }
}
