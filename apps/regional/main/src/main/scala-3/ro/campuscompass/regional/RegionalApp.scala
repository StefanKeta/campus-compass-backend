package ro.campuscompass.regional

import cats.effect.std.Random
import cats.effect.{ Async, Resource }
import cats.implicits.*
import ro.campuscompass.common.email.SMTPEmailInterpreter
import ro.campuscompass.common.logging.Logging
import ro.campuscompass.common.minio.MinIO
import ro.campuscompass.common.mongo.MongoDBClient
import ro.campuscompass.common.redis.RedisClient
import ro.campuscompass.regional.algebra.application.ApplicationAlgebra
import ro.campuscompass.regional.httpserver.RegionalServer
import ro.campuscompass.regional.algebra.authorization.AuthorizationAlgebra
import ro.campuscompass.regional.algebra.university.{ HousingCredentialsTemplate, UniversityAlgebra }
import ro.campuscompass.regional.persistance.{ ApplicationRepository, ProgramRepository }

object RegionalApp extends Logging {
  def apply[F[_]: Async]: Resource[F, Unit] = for {
    config          <- AppConfig.load[Resource[F, *]]
    given Random[F] <- Resource.eval(Random.scalaUtilRandom[F])
    redisCommands   <- RedisClient(config.redis)

    mongoClient <- MongoDBClient(config.mongo)
    mongoDb     <- Resource.eval(mongoClient.getDatabase(config.mongo.database))
    minio       <- Resource.eval(MinIO.apply(config.minio))

    programRepository     <- Resource.pure(ProgramRepository(mongoDb))
    applicationRepository <- Resource.pure(ApplicationRepository(mongoDb))

    emailAlgebra <- Resource.eval(SMTPEmailInterpreter[F](config.email))

    authAlgebra       <- Resource.pure(AuthorizationAlgebra[F](redisCommands, config.jwt))
    housingTemplate   <- Resource.eval(HousingCredentialsTemplate[F])
    universityAlgebra <- Resource.pure(UniversityAlgebra(emailAlgebra, housingTemplate, programRepository, applicationRepository))
    applicationAlgebra <- Resource.pure(ApplicationAlgebra(minio, applicationRepository))

    server <- RegionalServer.start(config.server)(
      authAlgebra,
      universityAlgebra,
      applicationAlgebra,
      config.regionalApiKey
    )
    _ <- logger[Resource[F, *]].info(s"Started server: ${server.address}")
  } yield ()
}
