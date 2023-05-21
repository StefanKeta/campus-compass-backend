package ro.campuscompass.global.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.common.domain
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.domain.error.AuthError
import ro.campuscompass.global.client.api.model.response.{ AppliedProgramme, UniversityProgramme, ViewApplicationRedirectDTO }
import ro.campuscompass.global.domain.University
import ro.campuscompass.global.domain.error.StudentError
import ro.campuscompass.global.httpserver.api.model.{ StudentApplicationDTO, ViewApplicationDTO }
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

import java.util.UUID

object StudentEndpoints {
  private val STUDENT_TAG = "Global student"

  def apply(): List[AnyEndpoint] =
    List(
      applyForProgrammeEndpoint,
      listProgrammesEndpoint,
      listAppliedProgrammesEndpoint,
      viewApplicationEndpoint,
      listUniversitiesEndpoint,
      listAppliedUniverstitiesEndpoint
    ).map(_.tag(STUDENT_TAG))

  private val baseStudentEndpoint = endpoint
    .in("api" / "v1" / "student")
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
        ),
        oneOfVariant(
          statusCode(StatusCode.BadRequest)
            .and(jsonBody[StudentError.ProgrammeNotFound])
        ),
        oneOfVariant(
          statusCode(StatusCode.BadRequest)
            .and(jsonBody[StudentError.ApplicationNotFound])
        ),
      )
    )

  val applyForProgrammeEndpoint: Endpoint[AuthToken, StudentApplicationDTO, AuthError | StudentError, Unit, Any] =
    baseStudentEndpoint.post
      .in("apply")
      .in(jsonBody[StudentApplicationDTO])
      .out(emptyOutput)

  val listProgrammesEndpoint: Endpoint[AuthToken, Unit, AuthError | StudentError, List[UniversityProgramme], Any] =
    baseStudentEndpoint.get
      .in("programmes")
      .out(jsonBody[List[UniversityProgramme]])

  val listAppliedProgrammesEndpoint: Endpoint[AuthToken, UUID, AuthError | StudentError, List[AppliedProgramme], Any] =
    baseStudentEndpoint.get
      .in("programmes" / path[UUID]("studentId"))
      .out(jsonBody[List[AppliedProgramme]])

  val viewApplicationEndpoint
    : Endpoint[AuthToken, ViewApplicationDTO, AuthError | StudentError, ViewApplicationRedirectDTO, Any] =
    baseStudentEndpoint.get
      .in("application")
      .in(jsonBody[ViewApplicationDTO]).out(jsonBody[ViewApplicationRedirectDTO])

  val listUniversitiesEndpoint: Endpoint[AuthToken, Unit, AuthError | StudentError, List[University], Any] =
    baseStudentEndpoint.get.in("universities").in(emptyInput).out(jsonBody[List[University]])

  val listAppliedUniverstitiesEndpoint: Endpoint[AuthToken, Unit, AuthError | StudentError, List[University], Any] =
    baseStudentEndpoint.get.in("applied-universities").in(emptyInput).out(jsonBody[List[University]])
}
