package ro.campuscompass.global.httpserver.route

import cats.effect.Async
import cats.effect.std.UUIDGen
import cats.implicits.*
import ro.campuscompass.common.crypto.JWT
import ro.campuscompass.common.domain.{ AuthToken, Role }
import ro.campuscompass.common.http.Routes
import ro.campuscompass.global.algebra.auth.AuthAlgebra
import ro.campuscompass.global.algebra.student.StudentAlgebra
import ro.campuscompass.global.httpserver.api.endpoint.StudentEndpoints
import ro.campuscompass.global.httpserver.api.endpoint.StudentEndpoints.*
import ro.campuscompass.global.httpserver.api.model.{
  AppliedProgrammeGlobalDTO,
  StudentApplicationDTO,
  StudentDataDTO,
  UniversityProgrammeGlobalDTO
}
import sttp.tapir.AnyEndpoint

import java.util.UUID

class StudentRoutes[F[_]: Async](authAlgebra: AuthAlgebra[F], studentAlgebra: StudentAlgebra[F])
  extends Routes[F] {
  override def routes = List(
    applyToProgrammeRoute,
    listUniversitiesRoute,
    listAppliedUniversitiesRoute,
    listAppliedProgrammesRoute,
    listProgrammesRoute,
    viewApplicationRoute,
    setStudentDetailsRoute,
    getStudentDetailsRoute
  )

  override def endpoints: List[AnyEndpoint] = StudentEndpoints()

  private val applyToProgrammeRoute =
    applyForProgrammeEndpoint.serverSecurityLogicRecoverErrors(handleAuthentication).serverLogicRecoverErrors(userId =>
      application =>
        for {
          _id <- UUIDGen.randomUUID[F]
          student = application.domain(_id, userId)
        } yield studentAlgebra.applyToProgramme(student)
    )

  private val listProgrammesRoute =
    listProgrammesEndpoint.serverSecurityLogicRecoverErrors(handleAuthentication).serverLogicRecoverErrors(_ =>
      _ => studentAlgebra.listProgrammes().map(_.map(prg => UniversityProgrammeGlobalDTO(prg)))
    )

  private val listAppliedProgrammesRoute =
    listAppliedProgrammesEndpoint.serverSecurityLogicRecoverErrors(handleAuthentication).serverLogicRecoverErrors(_ =>
      studentAlgebra.listAppliedProgrammes(_).map(_.map(app => AppliedProgrammeGlobalDTO(app)))
    )

  private val viewApplicationRoute =
    viewApplicationEndpoint.serverSecurityLogicRecoverErrors(handleAuthentication).serverLogicRecoverErrors(_ =>
      dto => studentAlgebra.viewApplication(dto.studentId, dto.universityId, dto.applicationId)
    )

  private val listUniversitiesRoute =
    listUniversitiesEndpoint.serverSecurityLogicRecoverErrors(handleAuthentication).serverLogicRecoverErrors(_ =>
      _ => studentAlgebra.listUniversities()
    )

  private val listAppliedUniversitiesRoute =
    listAppliedUniverstitiesEndpoint.serverSecurityLogicRecoverErrors(handleAuthentication).serverLogicRecoverErrors(userId =>
      _ => studentAlgebra.listAppliedUniversities(userId)
    )

  private val setStudentDetailsRoute = setStudentDetailsEndpoint
    .serverSecurityLogicRecoverErrors(handleAuthentication).serverLogicRecoverErrors(studentUserId =>
      studentData => studentAlgebra.setStudentData(studentUserId, studentData.domain(studentUserId))
    )

  private val getStudentDetailsRoute = getStudentDetailsEndpoint
    .serverSecurityLogicRecoverErrors(handleAuthentication).serverLogicRecoverErrors(studentId =>
      _ => studentAlgebra.getStudentData(studentId).map(StudentDataDTO.apply)
    )

  private def handleAuthentication(token: AuthToken): F[UUID] =
    authAlgebra.authenticate(JWT(token.value), Role.Student)

}

object StudentRoutes {
  def apply[F[_]: Async](authAlgebra: AuthAlgebra[F], studentAlgebra: StudentAlgebra[F]) =
    new StudentRoutes[F](authAlgebra, studentAlgebra)
}
