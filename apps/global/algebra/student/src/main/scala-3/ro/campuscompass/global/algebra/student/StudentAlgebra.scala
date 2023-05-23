package ro.campuscompass.global.algebra.student

import cats.{ Applicative, ApplicativeThrow }
import cats.effect.Sync
import cats.effect.std.UUIDGen
import cats.implicits.*
import org.http4s.client.Client
import org.typelevel.log4cats.Logger
import ro.campuscompass.common.logging.Logging
import ro.campuscompass.global.client.api.model.request.ViewApplicationReqDTO
import ro.campuscompass.global.domain.{
  AppliedProgrammeGlobal,
  Coordinates,
  StudentApplication,
  StudentData,
  University,
  UniversityProgrammeGlobal
}
import ro.campuscompass.global.domain.error.StudentError.AlreadyAppliedToUniversity
import ro.campuscompass.global.persistence.{ StudentApplicationRepository, StudentDataRepository, UniversityRepository }
import ro.campuscompass.global.client.api.model.response.{ AppliedProgramme, UniversityProgramme, ViewApplicationRedirectDTO }
import ro.campuscompass.global.client.client.StudentRegionalClient
import ro.campuscompass.global.client.config.RegionalConfig
import ro.campuscompass.global.domain.error.AdminError.UniversityNotFound
import ro.campuscompass.global.domain.error.StudentError
import ro.campuscompass.global.domain.error.StudentError.*

import java.util.UUID

trait StudentAlgebra[F[_]] {
  def applyToProgramme(studentApplication: StudentApplication): F[Unit]
  def listProgrammes(): F[List[UniversityProgrammeGlobal]]
  def listAppliedProgrammes(studentId: UUID): F[List[AppliedProgrammeGlobal]]
  def viewApplication(studentId: UUID, universityId: UUID, applicationId: UUID): F[ViewApplicationRedirectDTO]
  def listUniversities(): F[List[University]]
  def listAppliedUniversities(userId: UUID): F[List[University]]
  def getStudentData(studentId: UUID): F[StudentData]
  def setStudentData(studentUserId: UUID, studentData: StudentData): F[Unit]
}

object StudentAlgebra extends Logging {
  def apply[F[_]: Sync](
    applicationRepository: StudentApplicationRepository[F],
    studentRepository: StudentDataRepository[F],
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

      override def listProgrammes(): F[List[UniversityProgrammeGlobal]] = for {
        programmes <- client.listProgrammes()
        res <- programmes.traverse(programme =>
          for {
            university <- universityRepository.find(programme.uniUserId)
            programme <- ApplicativeThrow[F].fromOption(
              university,
              UniversityNotFound(s"University with userId ${programme.uniUserId}")
            ).map(uni =>
              UniversityProgrammeGlobal(
                uniUserId      = programme.uniUserId,
                programmeName  = programme.programmeName,
                degreeType     = programme.degreeType,
                universityName = uni.name
              )
            )
          } yield programme
        )
      } yield res

      override def listAppliedProgrammes(studentId: UUID): F[List[AppliedProgrammeGlobal]] =
        for {
          programmes <- client.listAppliedProgrammes(studentId)
          res <- programmes.traverse(programme =>
            for {
              university <- universityRepository.find(programme.uniUserId)
              programme <- ApplicativeThrow[F].fromOption(
                university,
                UniversityNotFound(s"University with userId ${programme.uniUserId}")
              ).map(uni =>
                AppliedProgrammeGlobal(
                  uniUserId      = programme.uniUserId,
                  applicationId  = programme.applicationId,
                  name           = programme.name,
                  degreeType     = programme.degreeType,
                  universityName = uni.name
                )
              )
            } yield programme
          )
        } yield res

      override def viewApplication(studentId: UUID, universityId: UUID, applicationId: UUID): F[ViewApplicationRedirectDTO] =
        for {
          node <- identifyNode(universityId)
          redirect <- client.viewApplication(ViewApplicationReqDTO(studentId, applicationId), node).recoverWith {
            case _ => ApplicativeThrow[F].raiseError(ApplicationNotFound(s"the application with id $applicationId not found!"))
          }
        } yield redirect

      override def listUniversities(): F[List[University]] = universityRepository.findAll()

      override def listAppliedUniversities(userId: UUID): F[List[University]] = for {
        applications <- applicationRepository.findAllApplications(userId)
        universityIds = applications.map(_.programmeId)
        universitiesApplied <- universityRepository.findByIds(universityIds)
      } yield universitiesApplied

      override def setStudentData(studentUserId: UUID, studentData: StudentData): F[Unit] =
        studentRepository.findById(studentUserId).flatMap {
          case Some(data) =>
            ApplicativeThrow[F].raiseError(
              StudentError.StudentDataExists(s"Student with id: $studentUserId already has data defined!")
            )
          case None => studentRepository.insert(studentUserId, studentData)
        }

      override def getStudentData(studentId: UUID): F[StudentData] = studentRepository.findById(studentId).flatMap { maybeData =>
        ApplicativeThrow[F].fromOption(maybeData, StudentDataDoesNotExist(s"Student with $studentId does not have defined data"))
      }

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
