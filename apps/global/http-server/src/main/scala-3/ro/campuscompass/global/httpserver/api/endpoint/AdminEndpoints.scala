package ro.campuscompass.global.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.global.domain.error.AdminError
import ro.campuscompass.global.httpserver.api.model.UniversityAdminDTO
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.model.*
import sttp.tapir.server.ServerEndpoint

import java.util.UUID

object AdminEndpoints {
  private val ADMIN_TAG = "Global admin"

  def apply(): List[AnyEndpoint] = List(
    listUniversitiesEndpoint,
    confirmExistenceEndpoint,
    rejectUniversityEndpoint
  )

  private val baseAdminEndpoint = endpoint
    .securityIn(auth.bearer[AuthToken]())
    .in("api" / "v1" / "admin")
    .tag(ADMIN_TAG)

  val listUniversitiesEndpoint: Endpoint[AuthToken, Unit, AdminError, List[UniversityAdminDTO], Any] =
    baseAdminEndpoint.get
      .in("list-universities")
      .out(jsonBody[List[UniversityAdminDTO]])
      .errorOut(
        oneOf[AdminError](
          oneOfVariant(
            statusCode(StatusCode.Unauthorized).and(
              jsonBody[AdminError.Unauthorized]
                .description("Unauthorized for the operation!")
            )
          )
        )
      )

  val confirmExistenceEndpoint: Endpoint[AuthToken, UUID, AdminError, Unit, Any] =
    baseAdminEndpoint.post
      .in("confirm" / path[UUID])
      .errorOut(
        oneOf[AdminError](
          oneOfVariant(
            statusCode(StatusCode.Unauthorized).and(jsonBody[AdminError.Unauthorized])
          ),
          oneOfVariant(
            statusCode(StatusCode.BadRequest).and(jsonBody[AdminError.UniversityNotFound])
          ),
          oneOfVariant(
            statusCode(StatusCode.BadRequest)
              .and(jsonBody[AdminError.UniversityAlreadyConfirmed])
          )
        )
      )

  val rejectUniversityEndpoint: Endpoint[AuthToken, UUID, AdminError, Unit, Any] = baseAdminEndpoint.post
    .in("reject" / path[UUID])
    .errorOut(
      oneOf[AdminError](
        oneOfVariant(
          statusCode(StatusCode.Unauthorized).and(jsonBody[AdminError.Unauthorized])
        ),
        oneOfVariant(
          statusCode(StatusCode.BadRequest).and(jsonBody[AdminError.UniversityNotFound])
        ),
        oneOfVariant(
          statusCode(StatusCode.BadRequest)
            .and(jsonBody[AdminError.UniversityAlreadyConfirmed])
        )
      )
    )
}
