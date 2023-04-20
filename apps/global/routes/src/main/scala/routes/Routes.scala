package routes

import sttp.tapir.server.ServerEndpoint

trait Routes[F[_]]{
  def routes:F[List[ServerEndpoint[Any,F]]]
}
