package routes.admin

import adminAlgebra.AdminAlgebra
import cats.effect.kernel.Async
import cats.{Applicative, MonadError}
import cats.implicits.*
import config.domain.AdminConfiguration
import dao.UniversityDAO
import domain.Entity.*
import error.AdminError
import endpoints.admin.AdminEndpoints
import error.AdminError.Unauthorized
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint.Full
 
class AdminRoutes[F[_]](adminConfiguration: AdminConfiguration,adminAlgebra: AdminAlgebra[F])(using F: Async[F]) {
  import AdminEndpoints._
  
  private val listUniversities: Full[UsernamePassword, Admin, Unit, AdminError, List[UniversityDAO], Any, F] =
    listUniversitiesEndpoint.serverSecurityLogicRecoverErrors(authenticateAdmin(_, adminConfiguration))
      .serverLogicRecoverErrors(_ => _ => adminAlgebra.getUniversities())
  
  private def authenticateAdmin(
      usernamePassword: UsernamePassword,
      adminConfiguration: AdminConfiguration
  ): F[Admin] =
    if(isAdmin(usernamePassword, adminConfiguration)) F.pure(Admin()) else F.raiseError(Unauthorized(s"You are not authorized to perform this request"))


  private def isAdmin(
      usernamePassword: UsernamePassword,
      adminConfiguration: AdminConfiguration
  ): Boolean =
    adminConfiguration.password == usernamePassword.password.getOrElse(
      ""
    ) && adminConfiguration.username == usernamePassword.username

    
  
  val routes = List(listUniversities)
}

object AdminRoutes{
  def apply[F[_]:Async](adminConfiguration: AdminConfiguration,adminAlgebra: AdminAlgebra[F]) = new AdminRoutes[F](adminConfiguration,adminAlgebra)
}
