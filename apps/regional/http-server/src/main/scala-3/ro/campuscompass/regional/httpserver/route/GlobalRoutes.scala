package ro.campuscompass.regional.httpserver.route

import cats.*
import cats.implicits.*
import cats.effect.implicits.*
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.domain.error.{ AuthError, GenericError }
import ro.campuscompass.common.http.Routes
import ro.campuscompass.regional.algebra.application.ApplicationAlgebra
import ro.campuscompass.regional.algebra.authorization.AuthorizationAlgebra
import ro.campuscompass.regional.algebra.university.UniversityAlgebra
import ro.campuscompass.regional.httpserver.api.endpoint.GlobalEndpoints
import sttp.tapir.AnyEndpoint
import ro.campuscompass.regional.httpserver.api.model.{ StudentApplicationDTO, StudyProgramDTO, UpdateApplicationStatusDTO }
import sttp.tapir.server.ServerEndpoint

class GlobalRoutes[F[_]: MonadThrow](
  authAlgebra: AuthorizationAlgebra[F],
  universityAlgebra: UniversityAlgebra[F],
  applicationAlgebra: ApplicationAlgebra[F],
  regionalApiKey: String
) extends Routes[F] {
  import ro.campuscompass.regional.httpserver.api.endpoint.GlobalEndpoints._

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
    .serverLogicRecoverErrors(_ =>
      dto =>
        applicationAlgebra.createApplication(???)
    )

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
