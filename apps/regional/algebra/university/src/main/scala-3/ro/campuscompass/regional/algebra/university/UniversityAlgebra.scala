package ro.campuscompass.regional.algebra.university

import cats.effect.*
import cats.*
import cats.effect.std.Random
import cats.implicits.*
import cats.effect.implicits.*
import ro.campuscompass.common.email.*
import ro.campuscompass.regional.domain.*
import ro.campuscompass.regional.persistance.*
import ro.campuscompass.common.domain.Credentials

import java.util.UUID

trait UniversityAlgebra[F[_]] {
  def createProgram(program: StudyProgram): F[Unit]
  def programs(universityId: Option[UUID]): F[List[StudyProgram]]
  def applications(universityId: UUID): F[List[Application]]
  def updateApplicationStatus(applicationId: UUID, status: ApplicationStatus): F[Unit]
  def sendHousingCredentials(universityId: UUID): F[Unit]
}

object UniversityAlgebra {
  def apply[F[_]: Async: Random](
    housingRepository: HousingCredentialsFirebaseRepository[F],
    emailAlgebra: EmailAlgebra[F],
    programRepository: ProgramRepository[F],
    applicationRepository: ApplicationRepository[F]
  ): F[UniversityAlgebra[F]] = for {
    housingTemplate <- HousingCredentialsTemplate[F]
  } yield {
    new UniversityAlgebra[F] {

      override def createProgram(program: StudyProgram): F[Unit] =
        programRepository.insert(program)

      override def programs(universityId: Option[UUID]): F[List[StudyProgram]] =
        programRepository.findAll().map { prgs =>
          universityId.fold(prgs)(u => prgs.filter(_.universityId == u))
        }

      override def applications(universityId: UUID): F[List[Application]] =
        for {
          applications <- applicationRepository.findAll()
          programs     <- programs(Some(universityId)).map(_.map(_._id))
          uniApplications = applications.filter(app => programs.contains(app.programId))
        } yield uniApplications

      def updateApplicationStatus(applicationId: UUID, status: ApplicationStatus): F[Unit] =
        applicationRepository.updateStatus(applicationId, status)

      def sendHousingCredentials(universityId: UUID): F[Unit] =
        for {
          apps <- applications(universityId)
          _ <- fs2.Stream.emits(apps.filter(app => app.housing && app.sentHousingCredentials.contains(false)))
            .covary[F]
            .evalTap { app =>
              for {
                user     <- List.fill(6)(Random[F].nextAlphaNumeric).sequence.map(_.mkString)
                password <- List.fill(8)(Random[F].nextAlphaNumeric).sequence.map(_.mkString)
                _ <- housingRepository.insert(HousingCredentials(
                  studentId    = app.studentId,
                  credentials  = Credentials(user, password),
                  universityId = universityId
                ))
                _ <- applicationRepository.updateSentCredentials(app._id, Some(true))
                _ <- emailAlgebra.send(
                  EmailRequest(
                    EmailAddress.unsafe(emailAlgebra.config.sender),
                    List(EmailAddress.unsafe(app.email)),
                    Subject("Housing credentials"),
                    Content(housingTemplate(user, password, emailAlgebra.config.sender))
                  )
                )

              } yield ()
            }
            .compile
            .drain
        } yield ()
    }
  }
}
