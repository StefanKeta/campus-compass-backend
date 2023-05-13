package ro.campuscompass.regional.algebra.authorization

import cats.effect.*
import cats.*
import cats.implicits.*
import cats.effect.implicits.*
import dev.profunktor.redis4cats.RedisCommands
import ro.campuscompass.common.crypto.{ JWT, JwtConfig, JwtUtils }
import ro.campuscompass.common.domain.error.AuthError.*
import ro.campuscompass.common.domain.error.AuthError.InvalidJwt

import java.util.UUID
import concurrent.duration.{ DAYS, DurationInt }

trait AuthorizationAlgebra[F[_]] {
  def storeUniversityJWT(universityId: UUID): F[JWT]
  def storeStudentJWT(usedId: UUID, applicationId: UUID): F[JWT]
  def authorizeUniversity(jwt: JWT, universityId: UUID): F[Unit]
  def authorizeStudent(jwt: JWT, applicationId: UUID): F[Unit]
}

object AuthorizationAlgebra {
  def apply[F[_]: Sync](
    redis: RedisCommands[F, String, String],
    jwtConfig: JwtConfig
  ) = new AuthorizationAlgebra[F] {
    def storeUniversityJWT(universityId: UUID): F[JWT] =
      for {
        token <- JwtUtils.generateJwt(jwtConfig)(
          "universityId" -> universityId.toString
        )
        _ <- redis.setEx(s"$universityId", token.value, 1.day)
      } yield token

    def storeStudentJWT(studentId: UUID, applicationId: UUID): F[JWT] =
      for {
        token <- JwtUtils.generateJwt(jwtConfig)(
          "studentId" -> studentId.toString,
          "applicationId" -> applicationId.toString
        )
        _ <- redis.setEx(s"$studentId", token.value, 1.day)
      } yield token

    def authorizeUniversity(jwt: JWT, universityId: UUID): F[Unit] =
      for {
        jwtMac <- JwtUtils.verifyAndParseJwt(jwt, jwtConfig)
        data   <- JwtUtils.extractData(jwtMac)
        userId <- MonadThrow[F].fromOption(
          data.get("universityId").filter(_ == s"$universityId"),
          Unauthorized("You are not authorized to perform this call!")
        )
        _ <- redis.get(userId.show).flatMap(o =>
          MonadThrow[F].fromOption(
            o.filter(_ == jwt.value),
            Unauthorized("You are not authorized to perform this call or JWT expired!")
          )
        )
      } yield ()

    def authorizeStudent(jwt: JWT, applicationId: UUID): F[Unit] =
      for {
        jwtMac <- JwtUtils.verifyAndParseJwt(jwt, jwtConfig)
        data   <- JwtUtils.extractData(jwtMac)
        studentId <- MonadThrow[F].fromOption(
          data.get("studentId"),
          Unauthorized("You are not authorized to perform this call!")
        )
        _ <- MonadThrow[F].fromOption(
          data.get("applicationId").filter(_ == s"$applicationId"),
          Unauthorized("You are not authorized to perform this call!")
        ).void
        _ <- redis.get(studentId).flatMap(o =>
          MonadThrow[F].fromOption(
            o.filter(_ == jwt.value),
            Unauthorized("You are not authorized to perform this call or JWT expired!")
          )
        )
      } yield ()
  }
}
