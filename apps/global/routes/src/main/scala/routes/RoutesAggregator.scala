package routes

import cats.effect.kernel.Async
import config.domain.AdminConfiguration
import org.http4s.HttpRoutes
import routes.admin.AdminRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter

trait RoutesAggregator [F[_]]{
  def aggregate():F[HttpRoutes[F]]
}

object RoutesAggregator{
  def apply[F[_]:Async:Http4sServerInterpreter](adminRoutes: AdminRoutes[F]): RoutesAggregator[F] = new RoutesAggregator[F]:
    override def aggregate(): F[HttpRoutes[F]] = {
      val routes = adminRoutes.routes
      val swaggerAggregator = SwaggerAggregator.swaggerInterpreter[F]()
      Async[F].pure(summon[Http4sServerInterpreter[F]].toRoutes(routes ++ swaggerAggregator))
    }
}
