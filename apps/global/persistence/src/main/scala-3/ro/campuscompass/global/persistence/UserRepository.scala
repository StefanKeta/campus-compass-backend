package ro.campuscompass.global.persistence

import cats.effect.Sync
import cats.implicits.*
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter
import ro.campuscompass.global.domain.User
import ro.campuscompass.global.persistence.rep.UserRep

trait UserRepository[F[_]] {
  def insert(user: User): F[Unit]
  def findByUsername(username: String): F[Option[User]]
}

object UserRepository {
  def apply[F[_]: Sync](mongoDatabase: MongoDatabase[F]) = new UserRepository[F]:
    private val docs = mongoDatabase.getCollectionWithCodec[UserRep]("users")

    override def insert(user: User): F[Unit] =
      docs.flatMap(_.insertOne(UserRep(user)).void)

    override def findByUsername(username: String): F[Option[User]] =
      docs.flatMap(_.find(Filter.eq("username", username)).first.map(_.map(_.domain)))
}
