package ro.campuscompass.global

import cats.Applicative
import cats.effect.*
import cats.effect.implicits.*
import cats.effect.std.Random
import cats.implicits.*
import org.http4s.ember.client.EmberClientBuilder
import ro.campuscompass.common.domain.Role
import ro.campuscompass.common.email.SMTPEmailInterpreter
import ro.campuscompass.common.firebase.FirebaseClient
import ro.campuscompass.common.logging.*
import ro.campuscompass.common.mongo.MongoDBClient
import ro.campuscompass.common.redis.RedisClient
import ro.campuscompass.global.client.*
import ro.campuscompass.global.algebra.admin.AdminAlgebra
import ro.campuscompass.global.algebra.auth.AuthAlgebra
import ro.campuscompass.global.algebra.student.StudentAlgebra
import ro.campuscompass.global.algebra.university.UniversityAlgebra
import ro.campuscompass.global.client.client.*
import ro.campuscompass.global.domain.User
import ro.campuscompass.global.httpserver.GlobalServer
import ro.campuscompass.global.persistence.*

import java.time.Instant
import java.util.UUID

object GlobalApp extends Logging {
  def apply[F[_]: Async]: Resource[F, Unit] = for {
    config <- AppConfig.load[Resource[F, *]]

    given Random[F] <- Resource.eval(Random.scalaUtilRandom[F])
    client          <- EmberClientBuilder.default[F].build

    mongoClient   <- MongoDBClient(config.mongo)
    mongoDb       <- Resource.eval(mongoClient.getDatabase(config.mongo.database))
    redisCommands <- RedisClient(config.redis)
    firestore     <- FirebaseClient.initializeFirebaseDb[F](config.firebase)

    userRepository               <- Resource.pure(UserRepository[F](mongoDb))
    universityRepository         <- Resource.pure(UniversityRepository[F](mongoDb))
    studentApplicationRepository <- Resource.pure(StudentApplicationRepository[F](mongoDb))
    studentDataRepository        <- Resource.pure(StudentDataRepository[F](mongoDb))
    universityFirebaseRepository <- Resource.pure(UniversityFirebaseRepository(firestore))

    _ <- Resource.eval {
      userRepository.findByUsername(config.admin.username).flatMap {
        case Some(value) => Applicative[F].unit
        case None => userRepository.insert(User(
            _id              = UUID.randomUUID(),
            username         = config.admin.username,
            password         = config.admin.password,
            role             = Role.Admin,
            registrationDate = Instant.now()
          ))
      }.map(_ => logger.info(s"Inserting user..."))
    }

    universityRegionalClient <-
      Resource.pure(UniversityRegionalClient[F](client, config.regional, config.regionalHosts, config.apiKey))
    studentRegionalClient <-
      Resource.pure(StudentRegionalClient[F](client, config.regional, config.regionalHosts, config.apiKey))

    emailAlgebra <- Resource.eval(SMTPEmailInterpreter[F](config.email))

    adminAlgebra <-
      Resource.pure(AdminAlgebra[F](userRepository, universityRepository, universityFirebaseRepository, emailAlgebra))
    authAlgebra <-
      Resource.pure(AuthAlgebra[F](
        userRepository,
        universityRepository,
        redisCommands,
        universityRegionalClient,
        config.jwt,
        config.regional,
        config.regionalHosts
      ))
    universityAlgebra <- Resource.pure(UniversityAlgebra[F](universityRepository))
    studentAlgebra <- Resource.pure(StudentAlgebra[F](
      studentApplicationRepository,
      studentDataRepository,
      universityRepository,
      studentRegionalClient,
      config.regional
    ))

    server <- GlobalServer.start(config.server)(
      adminAlgebra,
      authAlgebra,
      universityAlgebra,
      studentAlgebra
    )

    _ <- logger[Resource[F, *]].info(s"Started server: ${server.address}")
  } yield ()
}
