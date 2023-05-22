package ro.campuscompass.global.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.global.domain.error.UniversityError
import ro.campuscompass.global.httpserver.api.model.UniversitySignupDTO
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

object UniversityEndpoints {
  private val UNIVERSITY_TAG = "Global university"

  def apply(): List[AnyEndpoint] = List(enrollUniversityEndpoint)

  val enrollUniversityEndpoint: Endpoint[Unit, UniversitySignupDTO, UniversityError, Unit, Any] =
    endpoint
      .post
      .in("api" / "v1" / "enroll")
      .in(jsonBody[UniversitySignupDTO])
      .out(emptyOutput)
      .errorOut(
        oneOf[UniversityError](
          oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[UniversityError.UniversityEnrolled]))
        )
      )
      .tag(UNIVERSITY_TAG)

}
