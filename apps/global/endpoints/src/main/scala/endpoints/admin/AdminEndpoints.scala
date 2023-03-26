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
object AdminEndpoints {
  private val baseAdminEndpoint = endpoint.securityIn(auth.basic[UsernamePassword]()).in("api"/"v1"/ "admin").tag("Global admin")
  val listUniversitiesEndpoint
      : Endpoint[UsernamePassword, Unit, AdminError, List[UniversityDAO], Any] =
    baseAdminEndpoint
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

}
