package ro.campuscompass.global.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.global.domain.error.StudentError
import ro.campuscompass.global.httpserver.api.model.StudentApplicationDTO
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

object StudentEndpoints {
  private val STUDENT_TAG = "Global student"

  def apply(): List[AnyEndpoint] = List(applyForUniversityEndpoint)

  val applyForUniversityEndpoint: Endpoint[Unit, StudentApplicationDTO, StudentError, Unit, Any] =
    endpoint.post
      .in("api" / "v1" / "apply")
      .in(jsonBody[StudentApplicationDTO])
      .out(emptyOutput)
      .tag(STUDENT_TAG)
      .errorOut(
        oneOf[StudentError](
          oneOfVariant(
            statusCode(StatusCode.BadRequest)
              .and(jsonBody[StudentError.NonExistingUniversity])
          ),
          oneOfVariant(
            statusCode(StatusCode.BadRequest)
              .and(jsonBody[StudentError.EmailAlreadyEnrolled])
          ),
          oneOfVariant(
            statusCode(StatusCode.BadRequest)
              .and(jsonBody[StudentError.AlreadyAppliedToUniversity])
          )
        )
      )
}
