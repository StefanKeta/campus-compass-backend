package ro.campuscompass.regional.httpserver.api.endpoint

import io.circe.generic.auto.*
import ro.campuscompass.common.domain.AuthToken
import ro.campuscompass.common.domain.error.GenericError
import ro.campuscompass.regional.domain.*
import ro.campuscompass.regional.httpserver.api.model.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.model.*
import sttp.tapir.server.ServerEndpoint

import java.util.UUID

object UniversityEndpoints {
  private val REGIONAL_UNIVERSITY_TAG = "Regional university endpoints"

  def apply(): List[AnyEndpoint] =
    List(
      createProgram,
      listUniversityPrograms,
      listUniversityApplications,
      updateApplicationStatus,
      listUniversityHousingRequests,
      sendHousingCredentialsEndpoint
    )

  val createProgram: Endpoint[(AuthToken, UUID), CreateStudyProgramDTO, GenericError, StudyProgramDTO, Any] =
    endpoint
      .post
      .securityIn(auth.bearer[AuthToken]())
      .securityIn("api" / "v1" / "university" / path[UUID]("universityId") / "program")
      .in(jsonBody[CreateStudyProgramDTO])
      .out(jsonBody[StudyProgramDTO])
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[GenericError])
          )
        )
      )
      .tag(REGIONAL_UNIVERSITY_TAG)

  val listUniversityPrograms: Endpoint[(AuthToken, UUID), Unit, GenericError, List[StudyProgramDTO], Any] =
    endpoint
      .get
      .securityIn(auth.bearer[AuthToken]())
      .securityIn("api" / "v1" / "university" / path[UUID]("universityId") / "program")
      .out(jsonBody[List[StudyProgramDTO]])
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[GenericError])
          )
        )
      )
      .tag(REGIONAL_UNIVERSITY_TAG)

  val listUniversityApplications: Endpoint[(AuthToken, UUID), Unit, GenericError, List[Application], Any] =
    endpoint
      .get
      .securityIn(auth.bearer[AuthToken]())
      .securityIn("api" / "v1" / "university" / path[UUID]("universityId") / "applications")
      .out(jsonBody[List[Application]])
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[GenericError])
          )
        )
      )
      .tag(REGIONAL_UNIVERSITY_TAG)

  val updateApplicationStatus: Endpoint[(AuthToken, UUID), UpdateApplicationStatusDTO, GenericError, Unit, Any] =
    endpoint
      .put
      .securityIn(auth.bearer[AuthToken]())
      .securityIn("api" / "v1" / "university" / path[UUID]("universityId") / "application" / "status")
      .in(jsonBody[UpdateApplicationStatusDTO])
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[GenericError])
          )
        )
      )
      .tag(REGIONAL_UNIVERSITY_TAG)

  val listUniversityHousingRequests: Endpoint[(AuthToken, UUID), Unit, GenericError, List[HousingRequestDTO], Any] =
    endpoint
      .get
      .securityIn(auth.bearer[AuthToken]())
      .securityIn("api" / "v1" / "university" / path[UUID]("universityId") / "housing")
      .out(jsonBody[List[HousingRequestDTO]])
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError).and(jsonBody[GenericError])
          )
        )
      )
      .tag(REGIONAL_UNIVERSITY_TAG)

  val sendHousingCredentialsEndpoint: Endpoint[(AuthToken, UUID), Unit, GenericError, Unit, Any] =
    endpoint
      .post
      .securityIn(auth.bearer[AuthToken]())
      .securityIn("api" / "v1" / "university" / "housing" / path[UUID]("universityId"))
      .errorOut(
        oneOf[GenericError](
          oneOfVariant(
            statusCode(StatusCode.BadRequest).and(jsonBody[GenericError])
          )
        )
      )
}
