package ro.campuscompass.regional.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.regional.domain.*
import ro.campuscompass.regional.httpserver.api.model.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.model.*
import sttp.tapir.server.ServerEndpoint

object AuthorizationEndpoints {
  private val REGIONAL_AUTHORIZATION_TAG = "Regional authorization"

  def apply(): List[AnyEndpoint] = List(authorizeUniversity, authorizeStudent)

  val authorizeUniversity: Endpoint[String, AuthorizeUniversityDTO, AuthError.AuthUniversityError, AuthToken, Any] =
    endpoint
      .post
      .securityIn(auth.apiKey[String](header("X-Regional-Api-Key")))
      .in("api" / "v1" / "authorize" / "university")
      .in(jsonBody[AuthorizeUniversityDTO])
      .out(jsonBody[AuthToken])
      .errorOut(
        oneOf[AuthError.AuthUniversityError](
          oneOfVariant(
            statusCode(StatusCode.Unauthorized).and(jsonBody[AuthError.AuthUniversityError])
          )
        )
      )
      .tag(REGIONAL_AUTHORIZATION_TAG)
  
  val authorizeStudent: Endpoint[String, AuthorizeStudentDTO, AuthError.AuthStudentError, AuthToken, Any] =
    endpoint
      .post
      .securityIn(auth.apiKey[String](header("X-Regional-Api-Key")))
      .in("api" / "v1" / "authorize" / "student")
      .in(jsonBody[AuthorizeStudentDTO])
      .out(jsonBody[AuthToken])
      .errorOut(
        oneOf[AuthError.AuthStudentError](
          oneOfVariant(
            statusCode(StatusCode.Unauthorized).and(jsonBody[AuthError.AuthStudentError])
          )
        )
      )
      .tag(REGIONAL_AUTHORIZATION_TAG)

}
