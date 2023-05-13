package ro.campuscompass.global.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.domain.error.AuthError
import AuthError.StudentAlreadyEnrolled
import ro.campuscompass.global.httpserver.api.model.{LoginDTO, RegisterDTO}
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

object AuthEndpoints {
  private val GLOBAL_AUTHORIZATION_TAG = "Global authorization"

  def apply(): List[AnyEndpoint] = List(loginEndpoint,registerEndpoint)

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

  val registerEndpoint: Endpoint[Unit, RegisterDTO, AuthError, AuthToken, Any] = endpoint.post
    .in("api" / "v1" / "register")
    .in(jsonBody[RegisterDTO])
    .out(jsonBody[AuthToken])
    .errorOut(
      oneOf[AuthError](
        oneOfVariant(
          statusCode(StatusCode.BadRequest).and(jsonBody[StudentAlreadyEnrolled])
        )
      )
    )
    .tag(GLOBAL_AUTHORIZATION_TAG)
}
