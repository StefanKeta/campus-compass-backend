package ro.campuscompass.global.persistence

import cats.effect.Sync
import cats.implicits.*
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.{ Filter, Update }
import ro.campuscompass.global.domain.{ Coordinates, University }
import ro.campuscompass.global.persistence.rep.UniversityRep

import java.util.UUID

trait UniversityRepository[F[_]] {
  def insert(university: University): F[Unit]

  def find(universityId: UUID): F[Option[University]]

  def findByEmail(email: String): F[Option[University]]

  def findAll(): F[List[University]]

  def findByIds(ids: List[UUID]): F[List[University]]

  def findCoordinatesByUserId(userId: UUID): F[Option[Coordinates]]

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

    override def find(universityId: UUID): F[Option[University]] =
      docs.flatMap(_.find(Filter.eq[String]("_id", universityId.toString)).first.map(_.map(_.domain)))

    override def findByEmail(email: String): F[Option[University]] =
      docs.flatMap(_.find(Filter.eq("email", email)).first).map(_.map(_.domain))

    override def findCoordinatesByUserId(userId: UUID): F[Option[Coordinates]] =
      docs.flatMap(_.find(Filter.eq("userId", userId.toString)).first.map(_.map(_.coordinates)))

    override def updateUserId(_id: UUID, userId: UUID): F[Unit] =
      docs.flatMap(_.updateOne(Filter.eq("_id", _id.toString), Update.set("userId", userId.toString)).void)

    override def isConfirmed(_id: UUID): F[Option[Boolean]] =
      docs.flatMap(_.find(Filter.eq("_id", _id)).first.map(_.map(_.userId.isDefined)))

    override def findByIds(ids: List[UUID]): F[List[University]] =
      docs.flatMap(_.find(Filter.in[UUID]("_id", ids)).all).map(_.toList.map(_.domain))

    override def delete(_id: UUID): F[Unit] =
      docs.flatMap(_.deleteOne(Filter.eq[UUID]("_id", _id)).void)
}
