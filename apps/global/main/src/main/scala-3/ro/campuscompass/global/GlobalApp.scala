package ro.campuscompass.global

import cats.effect.*
import cats.effect.implicits.*
import cats.effect.std.Random
import cats.implicits.*
import ro.campuscompass.common.email.SMTPEmailInterpreter
import ro.campuscompass.common.logging.*
import ro.campuscompass.common.mongo.MongoDBClient
import ro.campuscompass.common.redis.RedisClient
import ro.campuscompass.global.algebra.admin.AdminAlgebra
import ro.campuscompass.global.algebra.auth.AuthAlgebra
import ro.campuscompass.global.algebra.student.StudentAlgebra
import ro.campuscompass.global.algebra.university.UniversityAlgebra
import ro.campuscompass.global.httpserver.GlobalServer
import ro.campuscompass.global.persistence.*

object GlobalApp extends Logging {
  def apply[F[_]: Async]: Resource[F, Unit] = for {
    config <- AppConfig.load[Resource[F, *]]

    given Random[F] <- Resource.eval(Random.scalaUtilRandom[F])

    mongoClient   <- MongoDBClient(config.mongo)
    mongoDb       <- Resource.eval(mongoClient.getDatabase(config.mongo.database))
    redisCommands <- RedisClient(config.redis)

    userRepository               <- Resource.pure(UserRepository[F](mongoDb))
    universityRepository         <- Resource.pure(UniversityRepository[F](mongoDb))
    studentApplicationRepository <- Resource.pure(StudentApplicationRepository[F](mongoDb))

    emailAlgebra <- Resource.eval(SMTPEmailInterpreter[F](config.email))

    adminAlgebra      <- Resource.pure(AdminAlgebra[F](userRepository, universityRepository, emailAlgebra))
    authAlgebra       <- Resource.pure(AuthAlgebra[F](userRepository, redisCommands, config.jwt))
    universityAlgebra <- Resource.pure(UniversityAlgebra[F](universityRepository))
    studentAlgebra    <- Resource.pure(StudentAlgebra[F](studentApplicationRepository, universityRepository))

    server <- GlobalServer.start(config.server)(
      adminAlgebra,
      authAlgebra,
      universityAlgebra,
      studentAlgebra
    )

    _ <- logger[Resource[F, *]].info(s"Started server: ${server.address}")
  } yield ()
}
