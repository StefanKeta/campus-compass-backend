package ro.campuscompass.global.httpserver.route

import cats.Functor
import cats.effect.Async
import cats.implicits.*
import ro.campuscompass.global.algebra.auth.AuthAlgebra
import ro.campuscompass.global.httpserver.api.endpoint.AuthEndpoints
import ro.campuscompass.global.httpserver.api.endpoint.AuthEndpoints.loginEndpoint
import ro.campuscompass.global.httpserver.api.model.AuthToken
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

class AuthRoutes[F[_]: Functor](authAlgebra: AuthAlgebra[F]) extends Routes[F] {

  def endpoints: List[AnyEndpoint]         = AuthEndpoints()
  def routes: List[ServerEndpoint[Any, F]] = List(loginRoute)

  private val loginRoute = loginEndpoint.serverLogicRecoverErrors(input =>
    authAlgebra.login(input.username, input.password)
      .map(jwt => AuthToken(jwt.value))
  )
}

object AuthRoutes {
  def apply[F[_]: Functor](authAlgebra: AuthAlgebra[F]) =
    new AuthRoutes[F](authAlgebra)
}
