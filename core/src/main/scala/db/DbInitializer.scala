package db

import cats.MonadError
import cats.effect.Sync
import cats.implicits.*
import config.domain.*
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCredential, MongoDatabase, ServerAddress}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.jdk.CollectionConverters.*

trait DbInitializer[F[_]]{
  def initDb():F[MongoDatabase]
}

object DbInitializer{
  def apply[F[_]](dbConfiguration: DbConfiguration)(using F:Sync[F],logger: Logger[F]): DbInitializer[F] = new DbInitializer[F]:
    override def initDb(): F[MongoDatabase] = F.delay{
      val credentials = MongoCredential.createCredential(userName = dbConfiguration.user,password = dbConfiguration.password.toCharArray,database = dbConfiguration.database)
      MongoClient(
        MongoClientSettings.builder()
          .applyToClusterSettings((builder: ClusterSettings.Builder) => builder.hosts(List(new ServerAddress(dbConfiguration.host, dbConfiguration.port)).asJava))
          .credential(credentials)
          .build())
          .getDatabase(dbConfiguration.database)
    }.flatTap { db =>
      summon[Logger[F]].info(s"Loaded ${db.name} successfully!")
    }
}
