package ro.campuscompass.regional.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.domain.error.GenericError
import ro.campuscompass.regional.domain.*
import ro.campuscompass.regional.httpserver.api.model.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.model.*
import sttp.tapir.server.ServerEndpoint

import java.util.UUID

object ApplicationEndpoints {
  private val REGIONAL_APPLICATION_TAG = "Regional application endpoints"

  def apply(): List[AnyEndpoint] =
    List(
      getApplication,
      submitApplication,
      uploadZip
    )

  val getApplication: Endpoint[(AuthToken, UUID), Unit, GenericError, Application, Any] =
    endpoint
      .get
      .securityIn(auth.bearer[AuthToken]())
      .securityIn("api" / "v1" / "application" / path[UUID]("applicationId"))
      .out(jsonBody[Application])
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[GenericError])
          )
        )
      )
      .tag(REGIONAL_APPLICATION_TAG)

  val submitApplication: Endpoint[(AuthToken, UUID), Unit, GenericError, Unit, Any] =
    endpoint
      .get
      .securityIn(auth.bearer[AuthToken]())
      .securityIn("api" / "v1" / "application" / path[UUID]("applicationId") / "submit")
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[GenericError])
          )
        )
      )
      .tag(REGIONAL_APPLICATION_TAG)

  val uploadZip: Endpoint[(AuthToken, UUID), ZipDTO, GenericError, Unit, Any] =
    endpoint
      .post
      .securityIn(auth.bearer[AuthToken]())
      .securityIn("api" / "v1" / "application" / path[UUID]("applicationId") / "upload")
      .in(multipartBody[ZipDTO])
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[GenericError])
          )
        )
      )
      .tag(REGIONAL_APPLICATION_TAG)
}
