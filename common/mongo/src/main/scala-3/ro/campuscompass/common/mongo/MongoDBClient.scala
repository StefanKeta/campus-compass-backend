package ro.campuscompass.common.mongo

import cats.*
import cats.effect.kernel.{ Async, Resource }
import cats.implicits.*
import mongo4cats.client.MongoClient
import mongo4cats.models.client.{ ConnectionString, MongoClientSettings, MongoConnection, MongoCredential }
import org.bson.UuidRepresentation
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object MongoDBClient {
  def apply[F[_]: Async](
    config: MongoDBConfig
  ): Resource[F, MongoClient[F]] = {
    val connection = MongoConnection.classic(
      host       = config.host,
      port       = config.port,
      credential = Some(MongoCredential(config.user, config.password))
    )

    MongoClient.create(
      MongoClientSettings
        .builder()
        .applyConnectionString(
          ConnectionString(connection.toString)
        )
        .uuidRepresentation(UuidRepresentation.STANDARD)
        .build()
    )
  }
}
