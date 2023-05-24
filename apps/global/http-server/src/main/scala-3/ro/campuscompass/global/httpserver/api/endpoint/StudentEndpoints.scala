package ro.campuscompass.global.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.common.domain
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.domain.error.AuthError
import ro.campuscompass.global.client.api.model.response.{AppliedProgramme, ViewApplicationRedirectDTO}
import ro.campuscompass.global.domain.University
import ro.campuscompass.global.domain.error.{AdminError, StudentError}
import ro.campuscompass.global.httpserver.api.model.{AppliedProgrammeGlobalDTO, ProgrammeApplicationDTO, StudentDataDTO, UniversityProgrammeGlobalDTO, ViewApplicationDTO}
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
      listAppliedUniverstitiesEndpoint,
      setStudentDetailsEndpoint,
      getStudentDetailsEndpoint
    ).map(_.tag(STUDENT_TAG))

  private val baseStudentEndpoint = endpoint
    .in("api" / "v1" / "student")
    .securityIn(auth.bearer[AuthToken]())
    .errorOut(
      oneOf[AuthError | StudentError | AdminError](
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
        oneOfVariant(
          statusCode(StatusCode.BadRequest)
            .and(jsonBody[StudentError.StudentDataExists])
        ),
        oneOfVariant(
          statusCode(StatusCode.BadRequest)
            .and(jsonBody[StudentError.StudentDataDoesNotExist])
        ),
        oneOfVariant(
          statusCode(StatusCode.BadRequest)
            .and(jsonBody[AdminError.UniversityNotFound])
        )
      )
    )

  val applyForProgrammeEndpoint: Endpoint[AuthToken, ProgrammeApplicationDTO, AuthError | StudentError | AdminError, Unit, Any] =
    baseStudentEndpoint.post
      .in("apply")
      .in(jsonBody[ProgrammeApplicationDTO])
      .out(emptyOutput)

  val listProgrammesEndpoint: Endpoint[AuthToken, Unit, AuthError | StudentError | AdminError, List[UniversityProgrammeGlobalDTO], Any] =
    baseStudentEndpoint.get
      .in("programmes")
      .out(jsonBody[List[UniversityProgrammeGlobalDTO]])

  val listAppliedProgrammesEndpoint: Endpoint[AuthToken, UUID, AuthError | StudentError | AdminError, List[AppliedProgrammeGlobalDTO], Any] =
    baseStudentEndpoint.get
      .in("programmes" / path[UUID]("studentId"))
      .out(jsonBody[List[AppliedProgrammeGlobalDTO]])

  val viewApplicationEndpoint
    : Endpoint[AuthToken, ViewApplicationDTO, AuthError | StudentError | AdminError, ViewApplicationRedirectDTO, Any] =
    baseStudentEndpoint.get
      .in("application")
      .in(jsonBody[ViewApplicationDTO]).out(jsonBody[ViewApplicationRedirectDTO])

  val listUniversitiesEndpoint: Endpoint[AuthToken, Unit, AuthError | StudentError | AdminError, List[University], Any] =
    baseStudentEndpoint.get.in("universities").in(emptyInput).out(jsonBody[List[University]])

  val listAppliedUniverstitiesEndpoint: Endpoint[AuthToken, Unit, AuthError | StudentError | AdminError, List[University], Any] =
    baseStudentEndpoint.get.in("applied-universities").in(emptyInput).out(jsonBody[List[University]])

  val setStudentDetailsEndpoint: Endpoint[AuthToken, StudentDataDTO, AuthError | StudentError | AdminError, Unit, Any] =
    baseStudentEndpoint
      .post
      .in("details")
      .in(jsonBody[StudentDataDTO])
      .out(emptyOutput)

  val getStudentDetailsEndpoint: Endpoint[AuthToken, Unit, AuthError | StudentError | AdminError, StudentDataDTO, Any] =
    baseStudentEndpoint
      .get
      .in("details")
      .in(emptyInput)
      .out(jsonBody[StudentDataDTO])
}
