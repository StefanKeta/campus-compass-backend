package ro.campuscompass.global.httpserver.route

import cats.*
import cats.effect.Async
import cats.implicits.*
import ro.campuscompass.common.crypto.JWT
import ro.campuscompass.common.domain.{ Principal, Role }
import ro.campuscompass.global.algebra.admin.{ AdminAlgebra, AdminConfig }
import ro.campuscompass.global.algebra.auth.AuthAlgebra
import ro.campuscompass.global.domain.error.AdminError
import ro.campuscompass.global.httpserver.api.endpoint.AdminEndpoints
import ro.campuscompass.global.httpserver.api.endpoint.AdminEndpoints.*
import ro.campuscompass.global.httpserver.api.model.UniversityAdminDTO
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.{ AnyEndpoint, Endpoint }

import scala.reflect.ClassTag

class AdminRoutes[F[_]: MonadThrow](authAlgebra: AuthAlgebra[F], adminAlgebra: AdminAlgebra[F])
  extends Routes[F] {

  def endpoints: List[AnyEndpoint]         = AdminEndpoints()
  def routes: List[ServerEndpoint[Any, F]] = List(listUniversities, confirmExistence, rejectUniversity)

  private val confirmExistence =
    confirmExistenceEndpoint
      .serverSecurityLogicRecoverErrors(token => authAlgebra.authenticate(JWT.apply(token.value), Role.Admin))
      .serverLogicRecoverErrors(_ =>
        universityId => adminAlgebra.confirmExistence(universityId)
      )

  private val rejectUniversity =
    rejectUniversityEndpoint
      .serverSecurityLogicRecoverErrors(token => authAlgebra.authenticate(JWT(token.value), Role.Admin))
      .serverLogicRecoverErrors(_ => universityId => adminAlgebra.rejectUniversityApplication(universityId))

  private val listUniversities =
    listUniversitiesEndpoint
      .serverSecurityLogicRecoverErrors(token => authAlgebra.authenticate(JWT.apply(token.value), Role.Admin))
      .serverLogicRecoverErrors(_ =>
        _ =>
          for {
            universities <- adminAlgebra.getUniversities
          } yield universities.map(u =>
            (UniversityAdminDTO.apply _).tupled(Tuple.fromProductTyped(u))
          )
      )
}

object AdminRoutes {
  def apply[F[_]: MonadThrow](authAlgebra: AuthAlgebra[F], adminAlgebra: AdminAlgebra[F]) =
    new AdminRoutes[F](authAlgebra, adminAlgebra)
}
