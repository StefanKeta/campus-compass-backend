package ro.campuscompass.regional.httpserver.route

import cats.*
import cats.effect.implicits.*
import cats.implicits.*
import ro.campuscompass.common.domain.{ AuthToken, StudyProgramDTO }
import ro.campuscompass.common.domain.error.*
import ro.campuscompass.common.http.Routes
import ro.campuscompass.regional.algebra.application.ApplicationAlgebra
import ro.campuscompass.regional.algebra.authorization.AuthorizationAlgebra
import ro.campuscompass.regional.algebra.university.UniversityAlgebra
import ro.campuscompass.regional.domain.{ AuthError, * }
import ro.campuscompass.regional.httpserver.api.endpoint.GlobalEndpoints
import ro.campuscompass.regional.httpserver.api.model.*
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

import java.time.Instant
import java.util.UUID

class GlobalRoutes[F[_]: MonadThrow](
  authAlgebra: AuthorizationAlgebra[F],
  universityAlgebra: UniversityAlgebra[F],
  applicationAlgebra: ApplicationAlgebra[F],
  regionalApiKey: String
) extends Routes[F] {
  import ro.campuscompass.regional.httpserver.api.endpoint.GlobalEndpoints.*

  override def endpoints: List[AnyEndpoint] = GlobalEndpoints()

  override def routes: List[ServerEndpoint[Any, F]] =
    List(authorizeUniversityRoute, authorizeStudentRoute, listProgramsRoute, createApplicationRoute, listApplicationsRoute)

  private val authorizeUniversityRoute = authorizeUniversity
    .serverSecurityLogicRecoverErrors(apiKey =>
      ApplicativeThrow[F].raiseWhen(apiKey != regionalApiKey)(
        AuthError.Unauthorized("Invalid API key")
      )
    )
    .serverLogicRecoverErrors(_ =>
      input =>
        authAlgebra.storeUniversityJWT(input.universityId)
          .map(jwt => AuthToken(jwt.value))
    )

  private val authorizeStudentRoute = authorizeStudent
    .serverSecurityLogicRecoverErrors(apiKey =>
      ApplicativeThrow[F].raiseWhen(apiKey != regionalApiKey)(
        AuthError.Unauthorized("Invalid API key")
      )
    )
    .serverLogicRecoverErrors(_ =>
      input =>
        authAlgebra.storeStudentJWT(input.studentId, input.applicationId)
          .map(jwt => AuthToken(jwt.value))
    )

  private val listProgramsRoute = listPrograms
    .serverSecurityLogicRecoverErrors(apiKey =>
      ApplicativeThrow[F].raiseWhen(apiKey != regionalApiKey)(
        AuthError.Unauthorized("Invalid API key")
      )
    )
    .serverLogicRecoverErrors(_ =>
      _ =>
        universityAlgebra.programs(None).map(_.map(StudyProgramDTO(_)))
    )

  private val createApplicationRoute = createApplication
    .serverSecurityLogicRecoverErrors(apiKey =>
      ApplicativeThrow[F].raiseWhen(apiKey != regionalApiKey)(
        AuthError.Unauthorized("Invalid API key")
      )
    )
    .serverLogicRecoverErrors { _ => dto =>
      for {
        uni <- universityAlgebra.programs(None)
        programmeName <-
          MonadThrow[F].fromOption(uni.find(_._id == dto.programId), GenericError("Programme not found")).map(_.name)
        res <- applicationAlgebra.createApplication(Application(
          _id                    = UUID.randomUUID(),
          studentId              = dto.studentId,
          programId              = dto.programId,
          programName            = programmeName,
          zipFile                = None,
          status                 = ApplicationStatus.InProcess,
          housing                = false,
          sentHousingCredentials = None,
          timestamp              = Instant.now(),
          studentData            = dto.studentData
        ))
      } yield res
    }

  private val listApplicationsRoute = listApplications
    .serverSecurityLogicRecoverErrors {
      case (apiKey, studentId) =>
        ApplicativeThrow[F].raiseWhen(apiKey != regionalApiKey)(
          AuthError.Unauthorized("Invalid API key")
        ) *> Applicative[F].pure(studentId)
    }
    .serverLogicRecoverErrors(studentId =>
      _ =>
        for {
          apps     <- applicationAlgebra.getApplicationForStudent(studentId)
          programs <- universityAlgebra.programs(None)
          result <- apps.traverse { app =>
            for {
              program <- MonadThrow[F].fromOption(
                programs.find(_._id == app.programId),
                GenericError(s"Program with id ${app.programId} does not exist")
              )
            } yield StudentApplicationDTO(
              applicationId   = app._id,
              timestamp       = app.timestamp,
              universityId    = program.universityId,
              programName     = program.name,
              programKind     = program.kind,
              programLanguage = program.language
            )
          }
        } yield result
    )

}

object GlobalRoutes {
  def apply[F[_]: MonadThrow](
    authAlgebra: AuthorizationAlgebra[F],
    universityAlgebra: UniversityAlgebra[F],
    applicationAlgebra: ApplicationAlgebra[F],
    regionalApiKey: String
  ) =
    new GlobalRoutes[F](authAlgebra, universityAlgebra, applicationAlgebra, regionalApiKey)
}
