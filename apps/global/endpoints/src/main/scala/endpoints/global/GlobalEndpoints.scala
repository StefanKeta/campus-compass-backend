package endpoints.global

import domain.LoginInput
import domain.user.AuthenticationToken
import error.AuthError
import error.AuthError.WrongCredentials
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto.*

object GlobalEndpoints {
  private val GLOBAL_AUTHORIZATION_TAG = "Global authorization"
  val loginEndpoint = endpoint.post
    .in("api" / "v1"/"login")
    .in(jsonBody[LoginInput])
    .out(jsonBody[AuthenticationToken])
    .errorOut(
      oneOf[AuthError](
        oneOfVariant(
          statusCode(StatusCode.Unauthorized).and(jsonBody[WrongCredentials])
        )
      )
    )
    .tag(GLOBAL_AUTHORIZATION_TAG)
  
  val globalEndpoints = List(loginEndpoint)
}
