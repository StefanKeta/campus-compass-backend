package ro.campuscompass.global.persistence

import cats.effect.Sync
import cats.implicits.*
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter
import ro.campuscompass.global.domain.StudentApplication
import ro.campuscompass.global.persistence.rep.StudentApplicationRep

import java.util.UUID

trait StudentApplicationRepository[F[_]] {
  def insert(application: StudentApplication): F[Unit]
  def findApplication(userId: UUID, appliedTo: UUID): F[Option[StudentApplication]]
  def findAllApplications(userId: UUID): F[List[StudentApplication]]
}

object StudentApplicationRepository {
  def apply[F[_]: Sync](mongoDatabase: MongoDatabase[F]) = new StudentApplicationRepository[F]:
    private val docs = mongoDatabase.getCollectionWithCodec[StudentApplicationRep]("student-applications")
    override def insert(application: StudentApplication): F[Unit] =
      docs.flatMap(_.insertOne(StudentApplicationRep(application)).void)

    override def findApplication(userId: UUID, appliedTo: UUID): F[Option[StudentApplication]] =
      docs.flatMap(_.find(Filter.eq[UUID](
        "userId",
        userId
      ).and(Filter.eq[UUID]("universityId", appliedTo))).first).map(_.map(_.domain))

    override def findAllApplications(userId: UUID): F[List[StudentApplication]] =
      docs.flatMap(_.find(Filter.eq[UUID]("userId", userId)).all).map(_.toList.map(_.domain))
}
