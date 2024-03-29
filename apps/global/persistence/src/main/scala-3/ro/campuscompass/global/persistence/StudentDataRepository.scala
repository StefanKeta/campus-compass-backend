package ro.campuscompass.global.persistence

import cats.effect.kernel.Sync
import cats.implicits.*
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter
import ro.campuscompass.common.domain.StudentData
import ro.campuscompass.global.persistence.rep.StudentDataRep

import java.util.UUID

trait StudentDataRepository[F[_]] {
  def insert(studentId: UUID, studentData: StudentData): F[Unit]
  def findById(studentId: UUID): F[Option[StudentData]]
}

object StudentDataRepository {
  def apply[F[_]: Sync](mongoDatabase: MongoDatabase[F]) = new StudentDataRepository[F]:
    private val docs = mongoDatabase.getCollectionWithCodec[StudentDataRep]("student-data")
    override def insert(studentId: UUID, studentData: StudentData): F[Unit] =
      for {
        _ <- docs.flatMap(_.findOneAndDelete(Filter.eq("studentId", s"$studentId")))
        _ <- docs.flatMap(_.insertOne(StudentDataRep(studentId, studentData)))
      } yield ()

    override def findById(studentId: UUID): F[Option[StudentData]] =
      docs.flatMap(_.find(Filter.eq("studentId", s"$studentId")).first).map(_.map(_.domain()))
}
