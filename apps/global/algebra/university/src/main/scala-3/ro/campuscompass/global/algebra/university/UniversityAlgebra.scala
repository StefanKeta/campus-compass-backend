package ro.campuscompass.global.algebra.university

import cats.{ Applicative, ApplicativeThrow }
import cats.effect.std.UUIDGen
import cats.effect.{ Async, Sync }
import cats.implicits.*
import org.typelevel.log4cats.Logger
import ro.campuscompass.common.logging.Logging
import ro.campuscompass.global.domain.University
import ro.campuscompass.global.domain.error.UniversityError
import ro.campuscompass.global.persistence.UniversityRepository

import java.util.UUID

trait UniversityAlgebra[F[_]] {
  def enrollUniversity(university: University): F[Unit]
}

object UniversityAlgebra extends Logging {
  def apply[F[_]: Sync](universityRepository: UniversityRepository[F]) =
    new UniversityAlgebra[F]:
      override def enrollUniversity(university: University): F[Unit] = for {
        _ <- universityRepository.findByEmail(university.email).flatMap {
          case Some(uni) =>
            ApplicativeThrow[F].raiseError(
              UniversityError.UniversityEnrolled(s"University with email ${university.email} already applied")
            )
          case None => Applicative[F].unit
        }
        _id <- UUIDGen[F].randomUUID
        _   <- universityRepository.insert(university)
        _   <- logger.info(s"University ${university.name} enrolled into the system!")
      } yield ()
}
