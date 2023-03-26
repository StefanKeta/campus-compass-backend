package db

import cats.MonadError
import cats.effect.kernel.{Async, Resource}
import cats.implicits.*
import config.domain.*
import mongo4cats.client.MongoClient
import mongo4cats.database.MongoDatabase
import mongo4cats.models.client.{MongoConnection, MongoConnectionType, MongoCredential}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


trait DbInitializer[F[_]]{
  def initDb():Resource[F,MongoClient[F]]
}

object DbInitializer{
  def apply[F[_]](dbConfiguration: DbConfiguration)(using F:Async[F]): DbInitializer[F] = new DbInitializer[F]:
    override def initDb():Resource[F,MongoClient[F]] = for{
      connection <- Resource.pure[F,MongoConnection](MongoConnection.classic(host=dbConfiguration.host,port = dbConfiguration.port,credential = Some(MongoCredential(dbConfiguration.user,dbConfiguration.password))))
      client <- MongoClient.fromConnection(connection)
    } yield client
}
