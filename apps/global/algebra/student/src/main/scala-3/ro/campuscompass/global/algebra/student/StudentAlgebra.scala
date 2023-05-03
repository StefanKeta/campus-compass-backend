package ro.campuscompass.global.algebra.student

import cats.ApplicativeThrow
import cats.effect.Sync
import cats.effect.std.UUIDGen
import cats.implicits.*
import org.typelevel.log4cats.Logger
import ro.campuscompass.common.logging.Logging
import ro.campuscompass.global.domain.{ StudentApplication, University }
import ro.campuscompass.global.domain.error.StudentError.AlreadyAppliedToUniversity
import ro.campuscompass.global.persistence.{ StudentApplicationRepository, UniversityRepository }

import java.util.UUID

trait StudentAlgebra[F[_]] {
  def applyToUniversity(studentApplication: StudentApplication): F[Unit]
  def listUniversities(): F[List[University]]
  def listAppliedUniversities(userId: UUID): F[List[University]]
}

object StudentAlgebra extends Logging {
  def apply[F[_]: Sync](applicationRepository: StudentApplicationRepository[F], universityRepository: UniversityRepository[F]) =
    new StudentAlgebra[F]:
      override def applyToUniversity(studentApplication: StudentApplication): F[Unit] = for {
        maybeApplication <- applicationRepository.findApplication(studentApplication.userId, studentApplication.universityId)
        _ <- maybeApplication match
          case Some(_) => ApplicativeThrow[F].raiseError(
              AlreadyAppliedToUniversity(s"Student with id ${studentApplication.userId} already applied to desired university")
            )
          case None => applicationRepository.insert(studentApplication)
      } yield ()

      override def listUniversities(): F[List[University]] = universityRepository.findAll()

      override def listAppliedUniversities(userId: UUID): F[List[University]] = for {
        applications <- applicationRepository.findAllApplications(userId)
        universityIds = applications.map(_.universityId)
        universitiesApplied <- universityRepository.findByIds(universityIds)
      } yield universitiesApplied
}
