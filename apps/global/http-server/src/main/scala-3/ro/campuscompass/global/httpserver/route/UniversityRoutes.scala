package ro.campuscompass.global.httpserver.route

import cats.effect.*
import cats.effect.std.UUIDGen
import cats.implicits.*
import ro.campuscompass.global.algebra.university.UniversityAlgebra
import ro.campuscompass.global.domain.University
import ro.campuscompass.global.httpserver.api.endpoint.UniversityEndpoints
import ro.campuscompass.global.httpserver.api.endpoint.UniversityEndpoints.enrollUniversityEndpoint
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

class UniversityRoutes[F[_]: Sync](universityAlgebra: UniversityAlgebra[F]) extends Routes[F] {

  def endpoints: List[AnyEndpoint]         = UniversityEndpoints()
  def routes: List[ServerEndpoint[Any, F]] = List(enrollUniversityRoute)

  private val enrollUniversityRoute =
    enrollUniversityEndpoint.serverLogicRecoverErrors(i =>
      for {
        _id <- UUIDGen[F].randomUUID
        _ <- universityAlgebra.enrollUniversity(
          University(
            _id           = _id,
            name          = i.name,
            contactPerson = i.contactPerson,
            email         = i.email,
            coordinates   = i.coordinates
          )
        )
      } yield ()
    )
}

object UniversityRoutes {
  def apply[F[_]: Sync](universityAlgebra: UniversityAlgebra[F]) = new UniversityRoutes[F](universityAlgebra)
}
