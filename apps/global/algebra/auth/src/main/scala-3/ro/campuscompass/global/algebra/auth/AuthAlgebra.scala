package ro.campuscompass.global.algebra.auth

import cats.effect.std.UUIDGen
import cats.effect.{Async, Sync}
import cats.implicits.*
import cats.{Applicative, ApplicativeThrow, MonadThrow, Monoid}
import dev.profunktor.redis4cats.RedisCommands
import io.circe.Json
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger
import ro.campuscompass.common
import ro.campuscompass.common.crypto.{JWT, JwtConfig, JwtUtils, SCrypt}
import ro.campuscompass.common.domain.Principal.Student
import ro.campuscompass.common.domain.Role
import ro.campuscompass.common.logging.Logging
import ro.campuscompass.common.time.Time
import ro.campuscompass.global.domain.User
import ro.campuscompass.global.domain.error.AuthError
import ro.campuscompass.global.domain.error.AuthError.*
import ro.campuscompass.global.persistence.UserRepository

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

trait AuthAlgebra[F[_]] {
  def login(username: String, password: String): F[JWT]
  def register(email: String, password: String): F[JWT]
  def authenticate(jwt: JWT,role: Role): F[UUID]
}

object AuthAlgebra extends Logging {
  def apply[F[_]: Sync](
    userRepository: UserRepository[F],
    redis: RedisCommands[F, String, String],
    jwtConfig: JwtConfig
  ) = new AuthAlgebra[F]:

    override def login(username: String, password: String): F[JWT] = for {
      user <- userRepository.findByUsername(username)
      token <- user.fold {
        ApplicativeThrow[F].raiseError(WrongCredentials("Wrong credentials!"))
      } { user =>
        checkUserAuthorization(user, password).ifM(
          createJWT(user),
          logger.info(s"Wrong credentials entered!") *> ApplicativeThrow[F].raiseError(
            WrongCredentials("Wrong credentials!")
          )
        )
      }
    } yield token

    override def register(email: String, password: String): F[JWT] = for {
      user <- userRepository.findByUsername(email)
      insertedUser <- if (user.isEmpty) insertStudent(email, password)
      else
        ApplicativeThrow[F].raiseError(
          StudentAlreadyEnrolled(s"Student with the email:$email is already enrolled into the platform!")
        )
      token <- createJWT(insertedUser)
    } yield token

    override def authenticate(jwt: JWT, role: Role): F[UUID] = for {
      userData <- JwtUtils.verifyAndParseJwt(jwt, jwtConfig)
      userId      <- userData.body.getCustomF[F,UUID]("_id")
      userRole <- userData.body.getCustomF[F,Role]("role")
      _ <- redis.get(userId.show).map {
        case Some(_) => if (userRole == role) Applicative[F].unit
          else ApplicativeThrow[F].raiseError(UnauthorizedRole("You are not authorized to perform this call!"))
        case None => ApplicativeThrow[F].raiseError(InvalidJwt(s"The provided jwt does not exist!"))
      }
    } yield userId

    private def checkUserAuthorization(user: User, password: String): F[Boolean] =
      SCrypt
        .check(password, user.password)
        .ifM(Applicative[F].pure(true), MonadThrow[F].raiseError(WrongCredentials("Wrong credentials!")))

    private def createJWT(user: User): F[JWT] = for {
      jwtEntity <- ("user", (user._id, user.role).asJson).pure[F]
      token     <- JwtUtils.generateJwt(jwtEntity, jwtConfig)
      _         <- redis.setEx(s"${user._id}", token.value, 1.day)
    } yield token

    private def insertStudent(email: String, password: String): F[User] = for {
      uuid           <- UUIDGen[F].randomUUID
      hashedPassword <- SCrypt.hash[F](password)
      now <- Time.now
      user = User(uuid, email, hashedPassword, Role.Student,now)
      _ <- userRepository.insert(user)
    } yield user
}
