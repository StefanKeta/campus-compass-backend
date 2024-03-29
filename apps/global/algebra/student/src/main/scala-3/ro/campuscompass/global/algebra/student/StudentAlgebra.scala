package ro.campuscompass.global.algebra.student

import cats.{ Applicative, ApplicativeThrow }
import cats.effect.Sync
import cats.effect.std.UUIDGen
import cats.implicits.*
import org.http4s.client.Client
import org.typelevel.log4cats.Logger
import ro.campuscompass.common.domain.StudentData
import ro.campuscompass.common.logging.Logging
import ro.campuscompass.global.client.api.model.request.ViewApplicationReqDTO
import ro.campuscompass.global.client.api.model.response.*
import ro.campuscompass.global.client.client.StudentRegionalClient
import ro.campuscompass.global.client.config.RegionalConfig
import ro.campuscompass.global.domain.*
import ro.campuscompass.global.domain.error.AdminError.UniversityNotFound
import ro.campuscompass.global.domain.error.StudentError
import ro.campuscompass.global.domain.error.StudentError.*
import ro.campuscompass.global.persistence.*

import java.util.UUID

trait StudentAlgebra[F[_]] {
  def applyToProgramme(application: ProgrammeApplication): F[Unit]
  def listProgrammes(): F[List[UniversityProgrammeGlobal]]
  def listAppliedProgrammes(studentId: UUID): F[List[AppliedProgrammeGlobal]]
  def viewApplication(studentId: UUID, universityId: UUID, applicationId: UUID): F[ViewApplicationRedirectDTO]
  def listUniversities(): F[List[University]]
  def getStudentData(studentId: UUID): F[StudentData]
  def setStudentData(studentUserId: UUID, studentData: StudentData): F[Unit]
}

object StudentAlgebra extends Logging {
  def apply[F[_]: Sync](
    studentRepository: StudentDataRepository[F],
    universityRepository: UniversityRepository[F],
    client: StudentRegionalClient[F],
    regionalConfig: RegionalConfig
  ) =
    new StudentAlgebra[F]:
      override def applyToProgramme(application: ProgrammeApplication): F[Unit] = for {
        node           <- identifyNode(application.universityUserId)
        studentDataOpt <- studentRepository.findById(application.studentId)
        studentData    <- ApplicativeThrow[F].fromOption(studentDataOpt, StudentDataDoesNotExist("Student data missing"))
        _ <- client.applyToProgramme(application, studentData, node).flatMap {
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
            university <- universityRepository.findByUserId(programme.universityId)
            programme <- ApplicativeThrow[F].fromOption(
              university,
              UniversityNotFound(s"University with userId ${programme.universityId}")
            ).map(uni =>
              UniversityProgrammeGlobal(
                uniUserId      = programme.universityId,
                programmeName  = programme.name,
                programmeId    = programme._id,
                degreeType     = programme.kind,
                language       = programme.language,
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
              university <- universityRepository.findByUserId(programme.uniUserId)
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
          redirect <- client.viewApplication(ViewApplicationReqDTO(studentId, applicationId), node)
        } yield redirect

      override def listUniversities(): F[List[University]] = for {
        universities <- universityRepository.findAll()
        l <- universities.traverse(u => universityRepository.isConfirmed(u._id).map(b => (u, b)))
          .map(_.filter(_._2.isDefined).filter(_._2.get).map(_._1))
      } yield l

      override def setStudentData(studentUserId: UUID, studentData: StudentData): F[Unit] =
        studentRepository.insert(studentUserId, studentData)

      override def getStudentData(studentId: UUID): F[StudentData] = studentRepository.findById(studentId).flatMap { maybeData =>
        ApplicativeThrow[F].fromOption(maybeData, StudentDataDoesNotExist(s"Student with $studentId does not have defined data"))
      }

      private def identifyNode(universityUserId: UUID) = for {
        coordinates <- universityRepository.findCoordinatesByUserId(universityUserId).flatMap(maybeCoord =>
          ApplicativeThrow[F].fromOption(maybeCoord, UniversityNotFound("No university found!"))
        )
        node <-
          Applicative[F].pure(regionalConfig.nodes.minBy(node => calculateDistance(coordinates, node.coordinates)))
      } yield node

      private def calculateDistance(coordinates: Coordinates, nodeCoordinates: Coordinates) = {
        val p1 = coordinates.lat
        val p2 = coordinates.lon
        val q1 = nodeCoordinates.lat
        val q2 = nodeCoordinates.lon
        math.sqrt(math.pow(q1 - p1, 2) + math.pow(q2 - p2, 2))
      }
}
