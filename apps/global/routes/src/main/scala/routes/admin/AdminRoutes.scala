package routes.admin

import adminAlgebra.AdminAlgebra
import cats.effect.Async
import cats.implicits.*
import config.domain.AdminConfiguration
import dao.UniversityDAO
import domain.user.Entity.*
import error.AdminError
import endpoints.admin.AdminEndpoints
import error.AdminError.Unauthorized
import routes.Routes
import sttp.tapir.Endpoint
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint.Full

import scala.reflect.ClassTag

class AdminRoutes[F[_]](adminConfiguration: AdminConfiguration,adminAlgebra: AdminAlgebra[F])(using F: Async[F]) extends Routes[F]{
  import AdminEndpoints._

  private val confirmExistence = handleAuthentication(confirmExistenceEndpoint).serverLogicRecoverErrors(admin => universityId => adminAlgebra.confirmExistence(admin, universityId))

  private val listUniversities: Full[UsernamePassword, Admin, Unit, AdminError, List[UniversityDAO], Any, F] =
    handleAuthentication(listUniversitiesEndpoint).serverLogicRecoverErrors(_ => _ => adminAlgebra.getUniversities())

  private def handleAuthentication[S<:UsernamePassword,I,E<:Throwable,O](endpoint: Endpoint[S,I,E,O,Any])(using classTag: ClassTag[E])= endpoint.serverSecurityLogicRecoverErrors(authenticateAdmin(_,adminConfiguration))
  
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
  
  val routes = Async[F].delay(List(listUniversities,confirmExistence))
}

object AdminRoutes{
  def apply[F[_]:Async](adminConfiguration: AdminConfiguration,adminAlgebra: AdminAlgebra[F]) = new AdminRoutes[F](adminConfiguration,adminAlgebra)
}
