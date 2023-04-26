package ro.campuscompass.global.httpserver.route

import cats.*
import cats.effect.Async
import cats.implicits.*
import ro.campuscompass.common.domain.Principal
import ro.campuscompass.global.algebra.admin.{ AdminAlgebra, AdminConfig }
import ro.campuscompass.global.domain.error.AdminError
import ro.campuscompass.global.httpserver.api.endpoint.AdminEndpoints
import ro.campuscompass.global.httpserver.api.endpoint.AdminEndpoints.*
import ro.campuscompass.global.httpserver.api.model.UniversityAdminDTO
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.{ AnyEndpoint, Endpoint }

import scala.reflect.ClassTag

class AdminRoutes[F[_]: MonadThrow](adminConfig: AdminConfig)(adminAlgebra: AdminAlgebra[F])
  extends Routes[F] {

  def endpoints: List[AnyEndpoint]         = AdminEndpoints()
  def routes: List[ServerEndpoint[Any, F]] = List(listUniversities, confirmExistence)

  private val confirmExistence =
    confirmExistenceEndpoint
      .serverSecurityLogicRecoverErrors(authenticateAdmin)
      .serverLogicRecoverErrors(_ =>
        universityId => adminAlgebra.confirmExistence(universityId)
      )

  private val listUniversities =
    listUniversitiesEndpoint
      .serverSecurityLogicRecoverErrors(authenticateAdmin)
      .serverLogicRecoverErrors(_ =>
        _ =>
          for {
            universities <- adminAlgebra.getUniversities
          } yield universities.map(u =>
            (UniversityAdminDTO.apply _).tupled(Tuple.fromProductTyped(u))
          )
      )

  private def authenticateAdmin(
    usernamePassword: UsernamePassword,
  ): F[Principal] = {
    val isAdmin = usernamePassword.password.contains(adminConfig.password) &&
      adminConfig.username == usernamePassword.username

    isAdmin.pure[F].ifM(
      Principal.Admin.pure[F],
      ApplicativeThrow[F].raiseError(AdminError.Unauthorized(s"You are not authorized to perform this request"))
    )
  }
}

object AdminRoutes {
  def apply[F[_]: MonadThrow](adminConfig: AdminConfig)(adminAlgebra: AdminAlgebra[F]) =
    new AdminRoutes[F](adminConfig)(adminAlgebra)
}
