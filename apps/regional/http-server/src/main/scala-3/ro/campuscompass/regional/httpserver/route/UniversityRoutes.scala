package ro.campuscompass.regional.httpserver.route

import cats.effect.std.UUIDGen
import cats.*
import cats.effect.kernel.Sync
import cats.implicits.*
import ro.campuscompass.common.crypto.JWT
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.domain.error.AuthError
import ro.campuscompass.common.http.Routes
import ro.campuscompass.regional.algebra.authorization.AuthorizationAlgebra
import ro.campuscompass.regional.algebra.university.UniversityAlgebra
import ro.campuscompass.regional.httpserver.api.endpoint.{ GlobalEndpoints, UniversityEndpoints }
import ro.campuscompass.regional.httpserver.api.model.{ HousingRequestDTO, StudyProgramDTO, UpdateApplicationStatusDTO }
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

class UniversityRoutes[F[_]: Sync](authAlgebra: AuthorizationAlgebra[F], universityAlgebra: UniversityAlgebra[F])
  extends Routes[F] {
  import ro.campuscompass.regional.httpserver.api.endpoint.UniversityEndpoints.*

  override def endpoints: List[AnyEndpoint] = UniversityEndpoints()

  override def routes: List[ServerEndpoint[Any, F]] =
    List(createProgramRoute, listUniversityProgramsRoute, listUniversityApplicationsRoute, updateApplicationStatusRoute)

  private val createProgramRoute = createProgram
    .serverSecurityLogicRecoverErrors {
      case (authToken, universityId) =>
        authAlgebra.authorizeUniversity(JWT(authToken.value), universityId) *> Applicative[F].pure(universityId)
    }
    .serverLogicRecoverErrors(universityId =>
      input =>
        for {
          _id <- UUIDGen.randomUUID[F]
          program = input.domain(_id, universityId)
          _ <- universityAlgebra.createProgram(program)
        } yield StudyProgramDTO(program)
    )

  private val listUniversityProgramsRoute = listUniversityPrograms
    .serverSecurityLogicRecoverErrors {
      case (authToken, universityId) =>
        authAlgebra.authorizeUniversity(JWT(authToken.value), universityId) *> Applicative[F].pure(universityId)
    }
    .serverLogicRecoverErrors(universityId =>
      _ =>
        universityAlgebra.programs(Some(universityId)).map(_.map(StudyProgramDTO(_)))
    )

  private val listUniversityApplicationsRoute = listUniversityApplications
    .serverSecurityLogicRecoverErrors {
      case (authToken, universityId) =>
        authAlgebra.authorizeUniversity(JWT(authToken.value), universityId) *> Applicative[F].pure(universityId)
    }
    .serverLogicRecoverErrors(universityId =>
      _ =>
        universityAlgebra.applications(universityId)
    )

  private val updateApplicationStatusRoute = updateApplicationStatus
    .serverSecurityLogicRecoverErrors {
      case (authToken, universityId) =>
        authAlgebra.authorizeUniversity(JWT(authToken.value), universityId) *> Applicative[F].pure(universityId)
    }
    .serverLogicRecoverErrors(_ =>
      input => universityAlgebra.updateApplicationStatus(input.applicationId, input.applicationStatus)
    )

  private val listUniversityHousingRequestsRoute = listUniversityHousingRequests
    .serverSecurityLogicRecoverErrors {
      case (authToken, universityId) =>
        authAlgebra.authorizeUniversity(JWT(authToken.value), universityId) *> Applicative[F].pure(universityId)
    }.serverLogicRecoverErrors(universityId =>
      _ =>
        universityAlgebra.applications(universityId)
          .map(_.filter(_.housing).map(app =>
            HousingRequestDTO(
              name                   = app.firstName + " " + app.lastName,
              applicationDate        = app.timestamp,
              sentHousingCredentials = app.sentHousingCredentials
            )
          ))
    )

}

object UniversityRoutes {
  def apply[F[_]: Sync](authAlgebra: AuthorizationAlgebra[F], universityAlgebra: UniversityAlgebra[F]) =
    new UniversityRoutes[F](authAlgebra, universityAlgebra)
}
