package endpoints.student
import domain.student.StudentApplication
import error.StudentError
import error.StudentError.*
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
object StudentEndpoints {
  private val STUDENT_TAG = "Global student"
  val applyForUniversityEndpoint
      : Endpoint[Unit, StudentApplication, StudentError, Unit, Any] =
    endpoint.post
      .in("api" / "v1" / "apply")
      .in(jsonBody[StudentApplication])
      .out(emptyOutput)
      .tag(STUDENT_TAG)
      .errorOut(
        oneOf[StudentError](
          oneOfVariant(
            statusCode(StatusCode.BadRequest)
              .and(jsonBody[NonExistingUniversity])
          ),
          oneOfVariant(
            statusCode(StatusCode.BadRequest)
              .and(jsonBody[EmailAlreadyEnrolled])
          ),
          oneOfVariant(
            statusCode(StatusCode.BadRequest)
              .and(jsonBody[AlreadyAppliedToUniversity])
          )
        )
      )

  val studentEndpoints = List(applyForUniversityEndpoint)
}
