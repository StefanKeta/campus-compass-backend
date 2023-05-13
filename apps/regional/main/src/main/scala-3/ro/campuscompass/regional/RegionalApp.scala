package ro.campuscompass.regional

import cats.effect.{ Async, Resource }
import cats.implicits.*
import ro.campuscompass.common.logging.Logging
import ro.campuscompass.common.redis.RedisClient
import ro.campuscompass.regional.httpserver.RegionalServer
import ro.campuscompass.regional.algebra.authorization.AuthorizationAlgebra

object RegionalApp extends Logging {
  def apply[F[_]: Async]: Resource[F, Unit] = for {
    config        <- AppConfig.load[Resource[F, *]]
    redisCommands <- RedisClient(config.redis)
    authAlgebra = AuthorizationAlgebra[F](redisCommands, config.jwt)
    server <- RegionalServer.start(config.server)(
      authAlgebra,
      config.regionalApiKey
    )
    _ <- logger[Resource[F, *]].info(s"Started server: ${server.address}")
  } yield ()
}
