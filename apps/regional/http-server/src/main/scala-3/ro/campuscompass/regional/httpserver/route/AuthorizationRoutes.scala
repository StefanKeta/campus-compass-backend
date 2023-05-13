package ro.campuscompass.regional.httpserver.route

import cats.*
import cats.implicits.*
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.domain.error.AuthError
import ro.campuscompass.common.http.Routes
import ro.campuscompass.regional.algebra.authorization.AuthorizationAlgebra
import ro.campuscompass.regional.httpserver.api.endpoint.AuthorizationEndpoints
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

class AuthorizationRoutes[F[_]: MonadThrow](authAlgebra: AuthorizationAlgebra[F], regionalApiKey: String)
  extends Routes[F] {
  import ro.campuscompass.regional.httpserver.api.endpoint.AuthorizationEndpoints._

  override def endpoints: List[AnyEndpoint] = AuthorizationEndpoints()

  override def routes: List[ServerEndpoint[Any, F]] = List(authorizeUniversityRoute, authorizeStudentRoute)

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

}

object AuthorizationRoutes {
  def apply[F[_]: MonadThrow](authAlgebra: AuthorizationAlgebra[F], regionalApiKey: String) =
    new AuthorizationRoutes[F](authAlgebra, regionalApiKey)
}
