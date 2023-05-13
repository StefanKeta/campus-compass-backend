package ro.campuscompass.global.httpserver

import cats.*
import cats.effect.*
import cats.implicits.*
import org.http4s.HttpRoutes
import ro.campuscompass.common.http.Routes
import ro.campuscompass.global.algebra.admin.{AdminAlgebra, AdminConfig}
import ro.campuscompass.global.algebra.auth.AuthAlgebra
import ro.campuscompass.global.algebra.student.StudentAlgebra
import ro.campuscompass.global.algebra.university.UniversityAlgebra
import ro.campuscompass.global.httpserver.route.{AdminRoutes, AuthRoutes, StudentRoutes, UniversityRoutes}
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

class GlobalServerRoutes[F[_]: Async](routes: List[Routes[F]]) {

  def http4sRoutes: HttpRoutes[F] = {
    val swaggerRoutes = SwaggerInterpreter().fromEndpoints[F](
      routes.flatMap(_.endpoints),
      "Global endpoints",
      "1.0"
    )
    Http4sServerInterpreter[F]().toRoutes(
      routes.flatMap(_.routes) ++ swaggerRoutes
    )
  }

}

object GlobalServerRoutes {
  def apply[F[_]: Async](
    adminAlgebra: AdminAlgebra[F],
    authAlgebra: AuthAlgebra[F],
    universityAlgebra: UniversityAlgebra[F],
    studentAlgebra: StudentAlgebra[F]
  ): GlobalServerRoutes[F] = new GlobalServerRoutes(
    List(
      AdminRoutes[F](authAlgebra, adminAlgebra),
      AuthRoutes[F](authAlgebra),
      UniversityRoutes[F](universityAlgebra),
      StudentRoutes[F](authAlgebra,studentAlgebra)
    )
  )
}
