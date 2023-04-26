package ro.campuscompass.global.persistence

import cats.effect.Sync
import cats.implicits.*
import mongo4cats.database.MongoDatabase
import ro.campuscompass.global.domain.StudentApplication
import ro.campuscompass.global.persistence.rep.StudentApplicationRep

trait StudentApplicationRepository[F[_]] {
  def insert(application: StudentApplication): F[Unit]
}

object StudentApplicationRepository {
  def apply[F[_]: Sync](mongoDatabase: MongoDatabase[F]) = new StudentApplicationRepository[F]:
    override def insert(application: StudentApplication): F[Unit] = for {
      docs <- mongoDatabase.getCollectionWithCodec[StudentApplicationRep]("student-applications")
      _    <- docs.insertOne(StudentApplicationRep(application))
    } yield ()
}
