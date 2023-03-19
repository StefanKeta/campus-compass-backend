package routes

import cats.effect.Async
import cats.implicits.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

class Routes[F[_]: Async] {
  import endpoints.Endpoints._
  val helloRoute = Http4sServerInterpreter[F]().toRoutes(
    helloEndpoint.serverLogicSuccess(_ => "Hello from CampusCompass!".pure[F])
  )
}
