package ro.campuscompass.global

import cats.effect.*
import cats.implicits.*
import pureconfig.*
import pureconfig.generic.derivation.default.*
import pureconfig.module.catseffect.syntax.*
import ro.campuscompass.common.crypto.JwtConfig
import ro.campuscompass.common.email.EmailConfig
import ro.campuscompass.common.http.ServerConfig
import ro.campuscompass.common.mongo.MongoDBConfig
import ro.campuscompass.common.redis.RedisConfig
import ro.campuscompass.global.algebra.admin.AdminConfig

final case class AppConfig(
  server: ServerConfig,
  admin: AdminConfig,
  email: EmailConfig,
  mongo: MongoDBConfig,
  redis: RedisConfig,
  jwt: JwtConfig
) derives ConfigReader

object AppConfig {
  def load[F[_]: Sync]: F[AppConfig] =
    for {
      configs <- ConfigSource.resources("resource.conf").loadF[F, AppConfig]()
    } yield configs
}
