package routes.global

import auth.AuthAlgebra
import cats.effect.Async
import routes.Routes

class GlobalRoutes[F[_]: Async](authAlgebra: AuthAlgebra[F]) extends Routes[F] {
  import endpoints.global.GlobalEndpoints.*

  private val loginRoute = loginEndpoint.serverLogicRecoverErrors(input =>
    authAlgebra.login(input.username, input.password)
  )
  def routes = Async[F].delay(List(loginRoute))
}

object GlobalRoutes {
  def apply[F[_]: Async](authAlgebra: AuthAlgebra[F]) =
    new GlobalRoutes[F](authAlgebra)
}
