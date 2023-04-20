package endpoints.admin

import error.AdminError
import error.AdminError.*
import dao.UniversityDAO
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.model.*
import io.circe.generic.auto.*
import mongo4cats.circe.*
import sttp.model.StatusCode

import java.util.UUID
object AdminEndpoints {
  private val ADMIN_TAG = "Global admin"
  private val baseAdminEndpoint = endpoint
    .securityIn(auth.basic[UsernamePassword]())
    .in("api" / "v1" / "admin")
    .tag(ADMIN_TAG)

  val listUniversitiesEndpoint
      : Endpoint[UsernamePassword, Unit, AdminError, List[UniversityDAO], Any] =
    baseAdminEndpoint.get
      .in("list-universities")
      .out(jsonBody[List[UniversityDAO]])
      .errorOut(
        oneOf[AdminError](
          oneOfVariant(
            statusCode(StatusCode.Unauthorized).and(
              jsonBody[Unauthorized]
                .description("Unauthorized for the operation!")
            )
          )
        )
      )

  val confirmExistenceEndpoint
      : Endpoint[UsernamePassword, UUID, AdminError, Unit, Any] =
    baseAdminEndpoint.post
      .in("confirm" / path[UUID])
      .errorOut(
        oneOf[AdminError](
          oneOfVariant(
            statusCode(StatusCode.Unauthorized).and(jsonBody[Unauthorized])
          ),
          oneOfVariant(
            statusCode(StatusCode.BadRequest).and(jsonBody[UniversityNotFound])
          ),
          oneOfVariant(
            statusCode(StatusCode.BadRequest)
              .and(jsonBody[UniversityAlreadyConfirmed])
          )
        )
      )

  val adminEndpoints = List(listUniversitiesEndpoint, confirmExistenceEndpoint)
}
