package ro.campuscompass.regional.httpserver.route

import cats.*
import cats.implicits.*
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.domain.error.AuthError
import ro.campuscompass.common.http.Routes
import ro.campuscompass.regional.algebra.authorization.AuthorizationAlgebra
import ro.campuscompass.regional.algebra.university.UniversityAlgebra
import ro.campuscompass.regional.httpserver.api.endpoint.GlobalEndpoints
import sttp.tapir.AnyEndpoint
import ro.campuscompass.regional.httpserver.api.model.{StudyProgramDTO, UpdateApplicationStatusDTO}
import sttp.tapir.server.ServerEndpoint

class GlobalRoutes[F[_]: MonadThrow](
  authAlgebra: AuthorizationAlgebra[F],
  universityAlgebra: UniversityAlgebra[F],
  regionalApiKey: String
) extends Routes[F] {
  import ro.campuscompass.regional.httpserver.api.endpoint.GlobalEndpoints._

  override def endpoints: List[AnyEndpoint] = GlobalEndpoints()

  override def routes: List[ServerEndpoint[Any, F]] = List(authorizeUniversityRoute, authorizeStudentRoute, listProgramsRoute)

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

}

object GlobalRoutes {
  def apply[F[_]: MonadThrow](
    authAlgebra: AuthorizationAlgebra[F],
    universityAlgebra: UniversityAlgebra[F],
    regionalApiKey: String
  ) =
    new GlobalRoutes[F](authAlgebra, universityAlgebra, regionalApiKey)
}
