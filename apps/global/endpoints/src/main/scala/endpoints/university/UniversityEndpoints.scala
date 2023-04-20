package endpoints.university

import domain.university.EnrollInput
import error.UniversityError
import error.UniversityError.SomeError
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

object UniversityEndpoints {
  private val UNIVERSITY_TAG = "Global university"
  val enrollUniversityEndpoint: Endpoint[Unit, EnrollInput, UniversityError, Unit, Any] =
    endpoint
      .post
      .in("api"/"v1"/ "enroll")
      .in(jsonBody[EnrollInput])
      .out(emptyOutput)
      .errorOut(
        oneOf[UniversityError](
          oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[SomeError])
          )
        )
      )
      .tag(UNIVERSITY_TAG)
    
  val universityEndpoints = List(enrollUniversityEndpoint)
}
