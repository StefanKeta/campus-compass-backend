package routes.university

import cats.effect.Async
import universityAlgebra.UniversityAlgebra

class UniversityRoutes[F[_]:Async](universityAlgebra:UniversityAlgebra[F]) {
  import endpoints.university.UniversityEndpoints.*

  private val enrollUniversityRoute = enrollUniversityEndpoint.serverLogicRecoverErrors(universityAlgebra.enrollUniversity)

  def routes = Async[F].delay(List(enrollUniversityRoute))
}

object UniversityRoutes{
  def apply[F[_]:Async](universityAlgebra: UniversityAlgebra[F]) = new UniversityRoutes[F](universityAlgebra)
}