package ro.campuscompass.global.algebra.student

import cats.{ Applicative, ApplicativeThrow }
import cats.effect.Sync
import cats.effect.std.UUIDGen
import cats.implicits.*
import org.http4s.client.Client
import org.typelevel.log4cats.Logger
import ro.campuscompass.common.logging.Logging
import ro.campuscompass.global.client.api.model.request.ViewApplicationReqDTO
import ro.campuscompass.global.domain.{ Coordinates, StudentApplication, University }
import ro.campuscompass.global.domain.error.StudentError.AlreadyAppliedToUniversity
import ro.campuscompass.global.persistence.{ StudentApplicationRepository, UniversityRepository }
import ro.campuscompass.global.client.api.model.response.{ AppliedProgramme, UniversityProgramme, ViewApplicationRedirectDTO }
import ro.campuscompass.global.client.client.StudentRegionalClient
import ro.campuscompass.global.client.config.RegionalConfig
import ro.campuscompass.global.domain.error.AdminError.UniversityNotFound
import ro.campuscompass.global.domain.error.StudentError.*

import java.util.UUID

trait StudentAlgebra[F[_]] {
  def applyToProgramme(studentApplication: StudentApplication): F[Unit]
  def listProgrammes(): F[List[UniversityProgramme]]
  def listAppliedProgrammes(studentId: UUID): F[List[AppliedProgramme]]
  def viewApplication(studentId: UUID, universityId: UUID, applicationId: UUID): F[ViewApplicationRedirectDTO]
  def listUniversities(): F[List[University]]
  def listAppliedUniversities(userId: UUID): F[List[University]]
}

object StudentAlgebra extends Logging {
  def apply[F[_]: Sync](
    applicationRepository: StudentApplicationRepository[F],
    universityRepository: UniversityRepository[F],
    client: StudentRegionalClient[F],
    regionalConfig: RegionalConfig
  ) =
    new StudentAlgebra[F]:
      override def applyToProgramme(studentApplication: StudentApplication): F[Unit] = for {
        node <- identifyNode(studentApplication.universityUserId)
        applicationId <- client.applyToProgramme(studentApplication, node).flatMap {
          case Some(id) => Applicative[F].pure(id)
          case None =>
            ApplicativeThrow[F].raiseError(
              ProgrammeNotFound("The selected programme was not found!")
            )
        }

      } yield ()

      override def listProgrammes(): F[List[UniversityProgramme]] = client.listProgrammes()

      override def listAppliedProgrammes(studentId: UUID): F[List[AppliedProgramme]] = client.listAppliedProgrammes(studentId)

      override def viewApplication(studentId: UUID, universityId: UUID, applicationId: UUID): F[ViewApplicationRedirectDTO] =
        for {
          node          <- identifyNode(universityId)
          maybeRedirect <- client.viewApplication(ViewApplicationReqDTO(studentId, applicationId), node)
          redirect <- ApplicativeThrow[F].fromOption(
            maybeRedirect,
            ApplicationNotFound(s"The application with $applicationId does not exist!")
          )
        } yield redirect

      override def listUniversities(): F[List[University]] = universityRepository.findAll()

      override def listAppliedUniversities(userId: UUID): F[List[University]] = for {
        applications <- applicationRepository.findAllApplications(userId)
        universityIds = applications.map(_.programmeId)
        universitiesApplied <- universityRepository.findByIds(universityIds)
      } yield universitiesApplied

      private def identifyNode(universityUserId: UUID) = for {
        coordinates <- universityRepository.findCoordinatesByUserId(universityUserId).flatMap(maybeCoord =>
          ApplicativeThrow[F].fromOption(maybeCoord, UniversityNotFound("No university found!"))
        )
        node <-
          Applicative[F].pure(regionalConfig.nodes.sortBy(node => calculateDistance(coordinates, node.coordinates)).reverse.head)
      } yield node

      private def calculateDistance(coordinates: Coordinates, nodeCoordinates: Coordinates) = {
        val p1 = coordinates.lat
        val p2 = coordinates.lon
        val q1 = nodeCoordinates.lat
        val q2 = nodeCoordinates.lon
        math.sqrt(math.pow(q1 - p1, 2) + math.pow(q2 - p2, 2))
      }
}
