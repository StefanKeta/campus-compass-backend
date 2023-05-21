package ro.campuscompass.regional.httpserver.route

import cats.*
import cats.effect.kernel.Sync
import cats.effect.std.UUIDGen
import cats.implicits.*
import ro.campuscompass.common.crypto.JWT
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.domain.error.AuthError
import ro.campuscompass.common.http.Routes
import ro.campuscompass.regional.algebra.application.ApplicationAlgebra
import ro.campuscompass.regional.algebra.authorization.AuthorizationAlgebra
import ro.campuscompass.regional.algebra.university.UniversityAlgebra
import ro.campuscompass.regional.httpserver.api.endpoint.*
import ro.campuscompass.regional.httpserver.api.model.{ HousingRequestDTO, StudyProgramDTO, UpdateApplicationStatusDTO }
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

import java.io.{ FileInputStream, InputStream }
import java.nio.file.Files

class ApplicationRoutes[F[_]: Sync](authAlgebra: AuthorizationAlgebra[F], applicationAlgebra: ApplicationAlgebra[F])
  extends Routes[F] {
  import ro.campuscompass.regional.httpserver.api.endpoint.ApplicationEndpoints.*

  override def endpoints: List[AnyEndpoint] = ApplicationEndpoints()

  override def routes: List[ServerEndpoint[Any, F]] =
    List(getApplicationRoute, submitApplicationRoute, uploadZipRoute)

  private val getApplicationRoute = getApplication
    .serverSecurityLogicRecoverErrors {
      case (authToken, applicationId) =>
        authAlgebra.authorizeStudent(JWT(authToken.value), applicationId) *> Applicative[F].pure(applicationId)
    }
    .serverLogicRecoverErrors(applicationId =>
      _ =>
        applicationAlgebra.getApplication(applicationId)
    )

  private val submitApplicationRoute = submitApplication
    .serverSecurityLogicRecoverErrors {
      case (authToken, applicationId) =>
        authAlgebra.authorizeStudent(JWT(authToken.value), applicationId) *> Applicative[F].pure(applicationId)
    }
    .serverLogicRecoverErrors(applicationId =>
      _ =>
        applicationAlgebra.submitApplication(applicationId)
    )

  private val uploadZipRoute = uploadZip
    .serverSecurityLogicRecoverErrors {
      case (authToken, applicationId) =>
        authAlgebra.authorizeStudent(JWT(authToken.value), applicationId) *> Applicative[F].pure(applicationId)
    }
    .serverLogicRecoverErrors(applicationId =>
      zip =>
        applicationAlgebra.uploadZip(
          applicationId,
          zip.zip.body,
          zip.zip.fileName
        )
    )

}

object ApplicationRoutes {
  def apply[F[_]: Sync](authAlgebra: AuthorizationAlgebra[F], applicationAlgebra: ApplicationAlgebra[F]) =
    new ApplicationRoutes[F](authAlgebra, applicationAlgebra)
}
