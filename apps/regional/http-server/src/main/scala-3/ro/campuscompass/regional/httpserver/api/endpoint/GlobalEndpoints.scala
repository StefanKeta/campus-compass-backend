package ro.campuscompass.regional.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.common.domain.{AuthorizeStudentDTO, AuthorizeUniversityDTO, AuthToken, CreateApplicationDTO}
import ro.campuscompass.common.domain.error.GenericError
import ro.campuscompass.regional.domain.*
import ro.campuscompass.regional.httpserver.api.model.*
import sttp.model.StatusCode
import sttp.tapir.{path, *}
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.model.*
import sttp.tapir.server.ServerEndpoint

import java.util.UUID

object GlobalEndpoints {
  private val REGIONAL_AUTHORIZATION_TAG = "Regional-global interactiong endpoints"

  def apply(): List[AnyEndpoint] = List(authorizeUniversity, authorizeStudent, listPrograms, createApplication, listApplications)

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

  val listPrograms: Endpoint[String, Unit, GenericError, List[StudyProgramDTO], Any] =
    endpoint
      .get
      .securityIn(auth.apiKey[String](header("X-Regional-Api-Key")))
      .securityIn("api" / "v1" / "program")
      .out(jsonBody[List[StudyProgramDTO]])
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[GenericError])
          )
        )
      )
      .tag(REGIONAL_AUTHORIZATION_TAG)

  val createApplication: Endpoint[String, CreateApplicationDTO, GenericError, UUID, Any] =
    endpoint
      .post
      .securityIn(auth.apiKey[String](header("X-Regional-Api-Key")))
      .securityIn("api" / "v1" / "application")
      .in(jsonBody[CreateApplicationDTO])
      .out(jsonBody[UUID])
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[GenericError])
          )
        )
      )
      .tag(REGIONAL_AUTHORIZATION_TAG)

  val listApplications: Endpoint[(String, UUID), Unit, GenericError, List[StudentApplicationDTO], Any] =
    endpoint
      .get
      .securityIn(auth.apiKey[String](header("X-Regional-Api-Key")))
      .securityIn("api" / "v1" / path[UUID]("studentId") / "application")
      .out(jsonBody[List[StudentApplicationDTO]])
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[GenericError])
          )
        )
      )
      .tag(REGIONAL_AUTHORIZATION_TAG)
}
