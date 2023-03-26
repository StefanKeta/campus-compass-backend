package routes

import cats.effect.Async
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object SwaggerAggregator {
  import endpoints.admin.AdminEndpoints._

  def swaggerInterpreter[F[_]:Async]() = SwaggerInterpreter().fromEndpoints[F](
    List(
      listUniversitiesEndpoint
    ),
    "Global endpoints",
    "1.0"
  )

}
