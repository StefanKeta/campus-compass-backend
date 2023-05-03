package ro.campuscompass.global.persistence

import cats.effect.Sync
import cats.implicits.*
import com.mongodb.client.model.Filters
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.{ Filter, Update }
import ro.campuscompass.global.domain.University
import ro.campuscompass.global.persistence.rep.UniversityRep

import java.util.UUID

trait UniversityRepository[F[_]] {
  def insert(university: University): F[Unit]

  def find(_id: UUID): F[Option[University]]

  def findAll(): F[List[University]]

  def findByIds(ids: List[UUID]): F[List[University]]

  def updateUserId(_id: UUID, userId: UUID): F[Unit]

  def isConfirmed(_id: UUID): F[Option[Boolean]]

  def delete(_id: UUID): F[Unit]
}

object UniversityRepository {
  def apply[F[_]: Sync](mongoDatabase: MongoDatabase[F]) = new UniversityRepository[F]:
    private val docs = mongoDatabase.getCollectionWithCodec[UniversityRep]("universities")

    override def insert(university: University): F[Unit] =
      docs.flatMap(_.insertOne(UniversityRep(university)).void)

    override def findAll(): F[List[University]] =
      docs.flatMap(_.find.all).map(_.toList.map(_.domain))

    override def find(_id: UUID): F[Option[University]] =
      docs.flatMap(_.find(Filters.eq("_id", _id)).first.map(_.map(_.domain)))

    override def updateUserId(_id: UUID, userId: UUID): F[Unit] =
      docs.flatMap(_.updateOne(Filter.eq("_id", _id), Update.set("userId", Some(userId))).void)

    override def isConfirmed(_id: UUID): F[Option[Boolean]] =
      docs.flatMap(_.find(Filters.eq("_id", _id)).first.map(_.map(_.userId.isDefined)))

    override def findByIds(ids: List[UUID]): F[List[University]] =
      docs.flatMap(_.find(Filter.in[UUID]("_id", ids)).all).map(_.toList.map(_.domain))

    override def delete(_id: UUID): F[Unit] =
      docs.flatMap(_.deleteOne(Filter.eq[UUID]("_id", _id)).void)
}
