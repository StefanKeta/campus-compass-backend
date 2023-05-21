package ro.campuscompass.regional.httpserver

import cats.*
import cats.effect.*
import cats.implicits.*
import org.http4s.HttpRoutes
import ro.campuscompass.common.http.Routes
import ro.campuscompass.regional.algebra.application.ApplicationAlgebra
import ro.campuscompass.regional.algebra.authorization.AuthorizationAlgebra
import ro.campuscompass.regional.algebra.university.UniversityAlgebra
import ro.campuscompass.regional.httpserver.route.{ ApplicationRoutes, GlobalRoutes, UniversityRoutes }
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

class RegionalServerRoutes[F[_]: Async](routes: List[Routes[F]]) {

  def http4sRoutes: HttpRoutes[F] = {
    val swaggerRoutes = SwaggerInterpreter().fromEndpoints[F](
      routes.flatMap(_.endpoints),
      "Regional endpoints",
      "1.0"
    )
    Http4sServerInterpreter[F]().toRoutes(
      routes.flatMap(_.routes) ++ swaggerRoutes
    )
  }

}

object RegionalServerRoutes {
  def apply[F[_]: Async](
    authAlgebra: AuthorizationAlgebra[F],
    universityAlgebra: UniversityAlgebra[F],
    applicationAlgebra: ApplicationAlgebra[F],
    regionalApiKey: String,
  ): RegionalServerRoutes[F] =
    new RegionalServerRoutes(
      List(
        GlobalRoutes(authAlgebra, universityAlgebra, applicationAlgebra, regionalApiKey),
        UniversityRoutes(authAlgebra, universityAlgebra),
        ApplicationRoutes(authAlgebra, applicationAlgebra)
      )
    )
}
