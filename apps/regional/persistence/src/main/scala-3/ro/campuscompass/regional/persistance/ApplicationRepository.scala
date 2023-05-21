package ro.campuscompass.regional.persistance

import io.circe.generic.auto.*
import mongo4cats.circe.*
import mongo4cats.codecs.MongoCodecProvider
import cats.effect.Sync
import cats.implicits.*
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.{ Filter, Update }
import ro.campuscompass.regional.domain.{ Application, ApplicationStatus, StudyProgram }
import ro.campuscompass.regional.persistance.rep.StudyProgramRep

import java.util.UUID

trait ApplicationRepository[F[_]] {
  def insert(application: Application): F[Unit]
  def findAll(): F[List[Application]]
  def updateStatus(applicationId: UUID, status: ApplicationStatus): F[Unit]
  def updateZipUrl(applicationId: UUID, url: String): F[Unit]
}

object ApplicationRepository {
  given MongoCodecProvider[Application] = deriveCirceCodecProvider

  def apply[F[_]: Sync](mongoDatabase: MongoDatabase[F]) = new ApplicationRepository[F]:
    private val docs = mongoDatabase.getCollectionWithCodec[Application]("applications")

    override def insert(application: Application): F[Unit] =
      docs.flatMap(_.insertOne(application).void)

    override def findAll(): F[List[Application]] =
      docs.flatMap(_.find.all).map(_.toList)

    override def updateStatus(applicationId: UUID, status: ApplicationStatus): F[Unit] =
      docs.flatMap(_.updateOne(Filter.eq("_id", applicationId), Update.set("status", status)).void)

    override def updateZipUrl(applicationId: UUID, url: String): F[Unit] =
      docs.flatMap(_.updateOne(Filter.eq("_id", applicationId), Update.set("zipFile", Some(url))).void)
}
