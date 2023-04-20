package routes.student

import cats.effect.Async
import endpoints.student.*
import routes.Routes
import studentAlgebra.StudentAlgebra

class StudentRoutes[F[_]: Async](studentAlgebra: StudentAlgebra[F])
    extends Routes[F] {
  import StudentEndpoints.*
  private val applyToUniRoute =
    applyForUniversityEndpoint.serverLogicRecoverErrors(studentAlgebra.applyToUni)

  def routes = Async[F].delay(List(applyToUniRoute))
}

object StudentRoutes {
  def apply[F[_]: Async](studentAlgebra: StudentAlgebra[F]) =
    new StudentRoutes[F](studentAlgebra)
}
