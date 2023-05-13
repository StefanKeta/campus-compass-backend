package ro.campuscompass.common.http

import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

trait Routes[F[_]] {
  def endpoints: List[AnyEndpoint]
  def routes: List[ServerEndpoint[Any, F]]
}
