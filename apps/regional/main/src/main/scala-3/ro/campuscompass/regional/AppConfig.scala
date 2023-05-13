package ro.campuscompass.regional

import cats.effect.*
import cats.implicits.*
import pureconfig.*
import pureconfig.generic.derivation.default.*
import pureconfig.module.catseffect.syntax.*
import ro.campuscompass.common.crypto.JwtConfig
import ro.campuscompass.common.http.ServerConfig
import ro.campuscompass.common.mongo.MongoDBConfig
import ro.campuscompass.common.redis.RedisConfig

final case class AppConfig(
  server: ServerConfig,
  regionalApiKey: String,
  // email: EmailConfig,
  // mongo: MongoDBConfig,
  redis: RedisConfig,
  jwt: JwtConfig
) derives ConfigReader

object AppConfig {
  def load[F[_]: Sync]: F[AppConfig] =
    for {
      configs <- ConfigSource.resources("resource.conf").loadF[F, AppConfig]()
    } yield configs
}
