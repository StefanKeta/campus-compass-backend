package ro.campuscompass.regional.algebra.university

import cats.{ Functor, Monad }
import cats.implicits.*
import ro.campuscompass.regional.domain.{ Application, ApplicationStatus, StudyProgram }
import ro.campuscompass.regional.persistance.{ ApplicationRepository, ProgramRepository }

import java.util.UUID

trait UniversityAlgebra[F[_]] {
  def createProgram(program: StudyProgram): F[Unit]
  def programs(universityId: Option[UUID]): F[List[StudyProgram]]
  def applications(universityId: UUID): F[List[Application]]
  def updateApplicationStatus(applicationId: UUID, status: ApplicationStatus): F[Unit]
}

object UniversityAlgebra {
  def apply[F[_]: Monad](
    programRepository: ProgramRepository[F],
    applicationRepository: ApplicationRepository[F]
  ): UniversityAlgebra[F] = new UniversityAlgebra[F]:
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
}
