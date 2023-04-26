package ro.campuscompass.global.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.global.domain.error.AuthError
import ro.campuscompass.global.httpserver.api.model.{ AuthToken, LoginDTO }
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

object AuthEndpoints {
  private val GLOBAL_AUTHORIZATION_TAG = "Global authorization"

  def apply(): List[AnyEndpoint] = List(loginEndpoint)

  val loginEndpoint: Endpoint[Unit, LoginDTO, AuthError, AuthToken, Any] = endpoint.post
    .in("api" / "v1" / "login")
    .in(jsonBody[LoginDTO])
    .out(jsonBody[AuthToken])
    .errorOut(
      oneOf[AuthError](
        oneOfVariant(
          statusCode(StatusCode.Unauthorized).and(jsonBody[AuthError.WrongCredentials])
        )
      )
    )
    .tag(GLOBAL_AUTHORIZATION_TAG)

}
