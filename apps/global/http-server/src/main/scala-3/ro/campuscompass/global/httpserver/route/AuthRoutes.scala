package ro.campuscompass.global.httpserver.route

import cats.Functor
import cats.effect.Async
import cats.implicits.*
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.http.Routes
import ro.campuscompass.global.algebra.auth.AuthAlgebra
import ro.campuscompass.global.httpserver.api.endpoint.AuthEndpoints
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

class AuthRoutes[F[_]: Functor](authAlgebra: AuthAlgebra[F]) extends Routes[F] {

  import ro.campuscompass.global.httpserver.api.endpoint.AuthEndpoints._
  
  def endpoints: List[AnyEndpoint]         = AuthEndpoints()
  def routes: List[ServerEndpoint[Any, F]] = List(loginRoute, registerRoute)

  private val loginRoute = loginEndpoint.serverLogicRecoverErrors(input =>
    authAlgebra.login(input.username, input.password)
  )

  private val registerRoute = registerEndpoint.serverLogicRecoverErrors(input =>
    authAlgebra.register(input.email, input.password)
  )
}

object AuthRoutes {
  def apply[F[_]: Functor](authAlgebra: AuthAlgebra[F]) =
    new AuthRoutes[F](authAlgebra)
}
