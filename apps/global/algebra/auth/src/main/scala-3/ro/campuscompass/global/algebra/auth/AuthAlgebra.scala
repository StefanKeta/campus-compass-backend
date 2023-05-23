package ro.campuscompass.global.algebra.auth

import cats.effect.std.UUIDGen
import cats.effect.{ Async, Sync }
import cats.implicits.*
import cats.{ Applicative, ApplicativeThrow, MonadThrow, Monoid }
import dev.profunktor.redis4cats.RedisCommands
import io.circe.Json
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.typelevel.log4cats.Logger
import ro.campuscompass.common
import ro.campuscompass.common.crypto.{ JWT, JwtConfig, JwtUtils, SCrypt }
import ro.campuscompass.common.domain.Role
import ro.campuscompass.common.domain.Role.{ Admin, University }
import ro.campuscompass.common.domain.error.AuthError
import ro.campuscompass.common.logging.Logging
import ro.campuscompass.common.time.Time
import ro.campuscompass.global.domain.error.AdminError.UniversityNotFound
import ro.campuscompass.common.domain.error.AuthError.*
import ro.campuscompass.global.domain.{ Coordinates, LoginResponseDTO, User }
import ro.campuscompass.global.persistence.{ UniversityRepository, UserRepository }
import ro.campuscompass.global.domain.User
import ro.campuscompass.global.client.client.UniversityRegionalClient
import ro.campuscompass.global.client.config.RegionalConfig
import ro.campuscompass.global.persistence.UserRepository

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

trait AuthAlgebra[F[_]] {
  def login(username: String, password: String): F[LoginResponseDTO]
  def register(email: String, password: String): F[JWT]
  def authenticate(jwt: JWT, role: Role): F[UUID]
}

object AuthAlgebra extends Logging {
  def apply[F[_]: Sync](
    userRepository: UserRepository[F],
    universityRepository: UniversityRepository[F],
    redis: RedisCommands[F, String, String],
    universityRegionalClient: UniversityRegionalClient[F],
    jwtConfig: JwtConfig,
    regionalConfig: RegionalConfig,
  ) = new AuthAlgebra[F]:

    override def login(username: String, password: String): F[LoginResponseDTO] = for {
      maybeUser     <- userRepository.findByUsername(username)
      user          <- ApplicativeThrow[F].fromOption(maybeUser, WrongCredentials("Wrong credentials!"))
      loginResponse <- generateLoginResponse(user)
    } yield loginResponse

    override def register(email: String, password: String): F[JWT] = for {
      user <- userRepository.findByUsername(email)
      insertedUser <- if (user.isEmpty) insertStudent(email, password)
      else
        ApplicativeThrow[F].raiseError(
          StudentAlreadyEnrolled(s"Student with the email:$email is already enrolled into the platform!")
        )
      jwt <- createJWT(insertedUser)
    } yield jwt

    override def authenticate(jwt: JWT, role: Role): F[UUID] = for {
      jwtMac <- JwtUtils.verifyAndParseJwt(jwt, jwtConfig)
      data   <- JwtUtils.extractData(jwtMac)
      userId <- MonadThrow[F].fromEither(data.get("userId").toRight(Unauthorized("You are not authorized to perform this call!")))
      userRole <- MonadThrow[F].fromEither(data.get("role").toRight(Unauthorized("You are not authorized to perform this call!")))
      _ <- redis.get(userId.show).map {
        case Some(_) => if (userRole == s"${role}") Applicative[F].unit
          else ApplicativeThrow[F].raiseError(Unauthorized("You are not authorized to perform this call!"))
        case None => ApplicativeThrow[F].raiseError(InvalidJwt(s"The provided jwt does not exist!"))
      }
    } yield UUID.fromString(userId)

    private def checkUserAuthorization(user: User, password: String): F[Boolean] =
      SCrypt
        .check(password, user.password)
        .ifM(Applicative[F].pure(true), MonadThrow[F].raiseError(WrongCredentials("Wrong credentials!")))

    private def createJWT(user: User): F[JWT] = for {
      token <- JwtUtils.generateJwt(jwtConfig)(
        "userId" -> s"${user._id}",
        "role" -> s"${user.role}"
      )
      _ <- redis.setEx(s"${user._id}", token.value, 1.day)
    } yield token

    private def insertStudent(email: String, password: String): F[User] = for {
      uuid           <- UUIDGen[F].randomUUID
      hashedPassword <- SCrypt.hash[F](password)
      now            <- Time.now
      user = User(uuid, email, hashedPassword, Role.Student, now)
      _ <- userRepository.insert(user)
    } yield user

    private def generateLoginResponse(user: User) = user.role match
      case University => for {
          node                 <- identifyNode(user._id)
          universityJwt        <- universityRegionalClient.generateUniversityUserJwt(user._id, node)
        } yield LoginResponseDTO(universityJwt, Some(node.fe))
      case _ => for {
          jwt <- createJWT(user)
        } yield LoginResponseDTO(jwt,None)

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
