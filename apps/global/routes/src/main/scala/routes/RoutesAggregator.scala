package routes

import cats.effect.kernel.Async
import config.domain.AdminConfiguration
import org.http4s.HttpRoutes
import routes.admin.AdminRoutes
import routes.global.GlobalRoutes
import routes.university.UniversityRoutes
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

trait RoutesAggregator [F[_]]{
  def aggregate():F[HttpRoutes[F]]
}

object RoutesAggregator{
  def apply[F[_]:Async:Http4sServerInterpreter](routes:List[ServerEndpoint[Any,F]]): RoutesAggregator[F] = new RoutesAggregator[F]:
    override def aggregate(): F[HttpRoutes[F]] = {
      val swaggerAggregator = SwaggerAggregator.swaggerInterpreter[F]()
      Async[F].pure(summon[Http4sServerInterpreter[F]].toRoutes(routes ++ swaggerAggregator))
    }
}
