package ro.campuscompass.regional.algebra.application

import cats.*
import cats.effect.Sync
import cats.implicits.*
import ro.campuscompass.common.domain.error.GenericError
import ro.campuscompass.common.minio.MinIO
import ro.campuscompass.regional.domain.*
import ro.campuscompass.regional.persistance.ApplicationRepository

import java.io.File
import java.util.UUID

trait ApplicationAlgebra[F[_]] {
  def createApplication(app: Application): F[UUID]

  def getApplication(applicationId: UUID): F[Application]

  def getApplicationForStudent(studentId: UUID): F[List[Application]]

  def submitApplication(applicationId: UUID, housing: Boolean): F[Unit]

  def uploadZip(applicationId: UUID, zipFile: File, fileName: Option[String]): F[Unit]
}

object ApplicationAlgebra {
  def apply[F[_]: Sync](
    minioClient: MinIO[F],
    applicationRepository: ApplicationRepository[F]
  ): ApplicationAlgebra[F] =
    new ApplicationAlgebra[F] {
      def createApplication(app: Application): F[UUID] =
        applicationRepository.insert(app) *> Sync[F].pure(app._id)

      def getApplication(applicationId: UUID): F[Application] = for {
        apps <- applicationRepository.findAll()
        app <-
          MonadThrow[F].fromOption(
            apps.find(_._id == applicationId),
            GenericError(s"Application with id ${applicationId} does not exists")
          )
      } yield app

      def getApplicationForStudent(studentId: UUID): F[List[Application]] =
        for {
          apps <- applicationRepository.findAll()
        } yield apps.filter(_.studentId == studentId)

      def submitApplication(applicationId: UUID, housing: Boolean): F[Unit] = for {
        app <- getApplication(applicationId)
        _ <- MonadThrow[F].fromOption(
          app.zipFile,
          GenericError(s"Documents were not loaded")
        )
        _ <- MonadThrow[F].raiseWhen(
          app.status != "InProcess"
        )(
          GenericError(s"Application $applicationId is in an inconsistent state")
        )
        _ <- applicationRepository.updateStatus(applicationId, "Submitted")

        _ <- if (housing)
          applicationRepository.updateHousing(applicationId, housing)
        else
          Sync[F].unit

        _ <- if (housing)
          applicationRepository.updateSentCredentials(applicationId, Some(false))
        else
          Sync[F].unit
      } yield ()

      def uploadZip(applicationId: UUID, zipFile: File, fileName: Option[String]): F[Unit] = for {
        _ <- minioClient.createBucketIfNotExists(s"$applicationId")
        _ <- minioClient.uploadWithOverwrite(
          s"$applicationId",
          fileName.getOrElse(s"$applicationId.zip"),
          zipFile
        )
        url <- minioClient.downloadLink(
          s"$applicationId",
          fileName.getOrElse(s"$applicationId.zip")
        )
        _ <- applicationRepository.updateZipUrl(applicationId, url)
      } yield ()

    }
}
