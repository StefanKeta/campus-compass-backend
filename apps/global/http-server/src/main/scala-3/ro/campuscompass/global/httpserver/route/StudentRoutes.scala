//package ro.campuscompass.global.httpserver.route
//
//import cats.effect.Async
//import ro.campuscompass.global.httpserver.api.endpoint.StudentEndpoints.applyForUniversityEndpoint
//
//class StudentRoutes[F[_]: Async](studentAlgebra: StudentAlgebra[F])
//  extends Routes[F] {
//
//  private val applyToUniversityRoute =
//    applyForUniversityEndpoint.serverLogicRecoverErrors(studentAlgebra.applyToUni)
//
//  def routes = Async[F].delay(List(applyToUniversityRoute))
//}
//
//object StudentRoutes {
//  def apply[F[_]: Async](studentAlgebra: StudentAlgebra[F]) =
//    new StudentRoutes[F](studentAlgebra)
//}
