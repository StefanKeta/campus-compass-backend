package ro.campuscompass.regional.persistance

import cats.effect.Sync
import cats.implicits.*
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter
import ro.campuscompass.regional.domain.StudyProgram
import ro.campuscompass.regional.persistance.rep.StudyProgramRep

import java.util.UUID

trait ProgramRepository[F[_]] {
  def insert(application: StudyProgram): F[Unit]
  def findAll(): F[List[StudyProgram]]
}

object ProgramRepository {
  def apply[F[_]: Sync](mongoDatabase: MongoDatabase[F]) = new ProgramRepository[F]:
    private val docs = mongoDatabase.getCollectionWithCodec[StudyProgramRep]("programs")

    override def insert(studyProgram: StudyProgram): F[Unit] =
      docs.flatMap(_.insertOne(StudyProgramRep(studyProgram)).void)

    override def findAll(): F[List[StudyProgram]] = 
      docs.flatMap(_.find.all).map(_.toList.map(_.domain))
}
