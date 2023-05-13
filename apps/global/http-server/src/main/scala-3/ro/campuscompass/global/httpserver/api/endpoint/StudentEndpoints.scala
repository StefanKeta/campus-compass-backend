package ro.campuscompass.global.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.domain.error.AuthError
import ro.campuscompass.global.domain.University
import ro.campuscompass.global.domain.error.StudentError
import ro.campuscompass.global.httpserver.api.model.StudentApplicationDTO
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

object StudentEndpoints {
  private val STUDENT_TAG = "Global student"

  def apply(): List[AnyEndpoint] =
    List(applyForUniversityEndpoint, listUniversitiesEndpoint, listAppliedUniverstitiesEndpoint).map(_.tag(STUDENT_TAG))

  private val baseStudentEndpoint = endpoint
    .in("api"/"v1"/ "student")
    .securityIn(auth.bearer[AuthToken]())
    .errorOut(
      oneOf[AuthError | StudentError](
        oneOfVariant(
          statusCode(StatusCode.Unauthorized)
            .and(jsonBody[AuthError.InvalidJwt])
        ),
        oneOfVariant(
          statusCode(StatusCode.Forbidden)
            .and(jsonBody[AuthError.Unauthorized])
        ),
        oneOfVariant(
          statusCode(StatusCode.BadRequest)
            .and(jsonBody[StudentError.NonExistingUniversity])
        ),
        oneOfVariant(
          statusCode(StatusCode.BadRequest)
            .and(jsonBody[StudentError.EmailAlreadyExists])
        ),
        oneOfVariant(
          statusCode(StatusCode.BadRequest)
            .and(jsonBody[StudentError.AlreadyAppliedToUniversity])
        )
      )
    )

  val applyForUniversityEndpoint: Endpoint[AuthToken, StudentApplicationDTO, AuthError | StudentError, Unit, Any] =
    baseStudentEndpoint.post
      .in("apply")
      .in(jsonBody[StudentApplicationDTO])
      .out(emptyOutput)

  val listUniversitiesEndpoint: Endpoint[AuthToken, Unit, AuthError | StudentError, List[University], Any] =
    baseStudentEndpoint.get.in("universities").in(emptyInput).out(jsonBody[List[University]])

  val listAppliedUniverstitiesEndpoint: Endpoint[AuthToken, Unit, AuthError | StudentError, List[University], Any] =
    baseStudentEndpoint.get.in("applied-universities").in(emptyInput).out(jsonBody[List[University]])
}
