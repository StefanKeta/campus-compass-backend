package ro.campuscompass.global.algebra.admin

import cats.*
import cats.effect.*
import cats.effect.std.{ Random, UUIDGen }
import cats.implicits.*
import cats.syntax.*
import io.circe.generic.auto.*
import mongo4cats.database.MongoDatabase
import org.typelevel.log4cats.Logger
import ro.campuscompass.common.crypto.*
import ro.campuscompass.common.domain.*
import ro.campuscompass.common.email.*
import ro.campuscompass.common.logging.Logging
import ro.campuscompass.global.domain.*
import ro.campuscompass.global.domain.error.AdminError.*
import ro.campuscompass.global.persistence.*

import java.util.UUID

trait AdminAlgebra[F[_]] {
  def getUniversities: F[List[University]]

  def confirmExistence(universityId: UUID): F[Unit]
}

object AdminAlgebra extends Logging {
  def apply[F[_]: Async: Random](
    userRepository: UserRepository[F],
    universityRepository: UniversityRepository[F],
    emailAlgebra: EmailAlgebra[F]
  ): F[AdminAlgebra[F]] =
    ConfirmUniversityTemplate[F].map { confirmUniversityTemplate =>

      new AdminAlgebra[F]:
        override def getUniversities: F[List[University]] = for {
          _            <- logger.info("Getting all the universities by admin")
          universities <- universityRepository.findAll()
          _            <- logger.info(s"Got ${universities.size} universities from database")
        } yield universities

        override def confirmExistence(universityId: UUID): F[Unit] = for {
          _           <- logger.info(s"Confirming university with $universityId...")
          isConfirmed <- universityRepository.isConfirmed(universityId)
          _ <- isConfirmed match
            case Some(isConfirmed) =>
              if (isConfirmed)
                logger.error("You cannot confirm the identity twice!") *> MonadThrow[F].raiseError(
                  UniversityAlreadyConfirmed("The university you are trying to get confirmation for already exists!")
                )
              else
                createUniversityUser(universityId)
            case None =>
              logger.error(s"University with id: $universityId does not exist!") *> ApplicativeThrow[F].raiseError(
                UniversityNotFound(s"University with id: $universityId does not exist!")
              )
          _ <- logger.info(s"University $universityId confirmed!")
        } yield ()

        private def createUniversityUser(universityId: UUID): F[Unit] = for {
          university <- universityRepository.find(universityId).flatMap(o =>
            ApplicativeThrow[F].fromOption(o, IllegalStateException("University does exits"))
          )
          usedId         <- UUIDGen[F].randomUUID
          credentials    <- generateCredentials()
          hashedPassword <- SCrypt.hash(credentials.password)
          _              <- userRepository.insert(User(usedId, credentials.username, hashedPassword, Role.University))
          _              <- universityRepository.updateUserId(universityId, usedId)
          _ <- emailAlgebra.send(EmailRequest(
            EmailAddress.unsafe(emailAlgebra.config.sender),
            List(EmailAddress.unsafe(university.email)),
            Subject("University confirmation"),
            Content(confirmUniversityTemplate(credentials.username, credentials.password, emailAlgebra.config.sender))
          ))
        } yield ()

        private def generateCredentials(): F[Credentials] = for {
          username <- Random[F].betweenInt(10_000_000, 999_999_999)
          password <- PasswordGenerator.generatePassword()
        } yield Credentials(username.toString, password)
    }

}
