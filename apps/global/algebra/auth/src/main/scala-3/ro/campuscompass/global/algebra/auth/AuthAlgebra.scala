package ro.campuscompass.global.algebra.auth

import cats.effect.{ Async, Sync }
import cats.implicits.*
import cats.{ Applicative, ApplicativeThrow, MonadThrow }
import dev.profunktor.redis4cats.RedisCommands
import io.circe.Json
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger
import ro.campuscompass.common.crypto.{ JWT, JwtConfig, JwtUtils, SCrypt }
import ro.campuscompass.common.logging.Logging
import ro.campuscompass.global.domain.User
import ro.campuscompass.global.domain.error.AuthError
import ro.campuscompass.global.persistence.UserRepository

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

trait AuthAlgebra[F[_]] {
  def login(username: String, password: String): F[JWT]
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
        ApplicativeThrow[F].raiseError(AuthError.WrongCredentials("Wrong credentials!"))
      } { user =>
        checkUserAuthorization(user, password).ifM(
          createJWT(user),
          logger.info(s"Wrong credentials entered!") *> ApplicativeThrow[F].raiseError(
            AuthError.WrongCredentials("Wrong credentials!")
          )
        )
      }

    } yield token

    private def checkUserAuthorization(user: User, password: String): F[Boolean] =
      SCrypt
        .check(password, user.password)
        .ifM(Applicative[F].pure(true), MonadThrow[F].raiseError(AuthError.WrongCredentials("Wrong credentials!")))

    private def createJWT(user: User): F[JWT] = for {
      jwtEntity <- ("user", (user._id, user.role).asJson).pure[F]
      token     <- JwtUtils.generateJwt(jwtEntity, jwtConfig)
      _         <- redis.setEx(s"${user._id}", token.value, 1.day)
    } yield token
}
