package ro.campuscompass.global.httpserver.route

import cats.effect.Async
import cats.effect.std.UUIDGen
import cats.implicits.*
import ro.campuscompass.common.crypto.JWT
import ro.campuscompass.common.domain.Role
import ro.campuscompass.common.http.Routes
import ro.campuscompass.global.algebra.auth.AuthAlgebra
import ro.campuscompass.global.algebra.student.StudentAlgebra
import ro.campuscompass.global.httpserver.api.endpoint.StudentEndpoints
import ro.campuscompass.global.httpserver.api.endpoint.StudentEndpoints.*
import ro.campuscompass.global.httpserver.api.model.StudentApplicationDTO
import sttp.tapir.AnyEndpoint

class StudentRoutes[F[_]: Async](authAlgebra: AuthAlgebra[F], studentAlgebra: StudentAlgebra[F])
  extends Routes[F] {
  override def routes = List(applyToProgrammeRoute, listUniversitiesRoute, listAppliedUniversitiesRoute)

  override def endpoints: List[AnyEndpoint] = StudentEndpoints()

  private val applyToProgrammeRoute =
    applyForProgrammeEndpoint.serverSecurityLogicRecoverErrors(token =>
      authAlgebra.authenticate(JWT(token.value), Role.Student)
    ).serverLogicRecoverErrors(userId =>
      application =>
        for {
          _id <- UUIDGen.randomUUID[F]
          student = application.domain(_id, userId)
        } yield studentAlgebra.applyToProgramme(student)
    )

  private val listAppliedProgrammesRoute = listAppliedProgrammesEndpoint.serverSecurityLogicRecoverErrors(token =>
    authAlgebra.authenticate(JWT(token.value), Role.Student)
  ).serverLogicRecoverErrors(_ => studentAlgebra.listAppliedProgrammes)

  private val viewApplicationRoute = viewApplicationEndpoint.serverSecurityLogicRecoverErrors(token =>
    authAlgebra.authenticate(JWT(token.value), Role.Student)
  ).serverLogicRecoverErrors(_ => dto => studentAlgebra.viewApplication(dto.studentId, dto.universityId, dto.applicationId))

  private val listUniversitiesRoute = listUniversitiesEndpoint.serverSecurityLogicRecoverErrors(token =>
    authAlgebra.authenticate(JWT(token.value), Role.Student)
  ).serverLogicRecoverErrors(_ => _ => studentAlgebra.listUniversities())

  private val listAppliedUniversitiesRoute = listAppliedUniverstitiesEndpoint.serverSecurityLogicRecoverErrors(token =>
    authAlgebra.authenticate(JWT(token.value), Role.Student)
  ).serverLogicRecoverErrors(userId => _ => studentAlgebra.listAppliedUniversities(userId))

}

object StudentRoutes {
  def apply[F[_]: Async](authAlgebra: AuthAlgebra[F], studentAlgebra: StudentAlgebra[F]) =
    new StudentRoutes[F](authAlgebra, studentAlgebra)
}
