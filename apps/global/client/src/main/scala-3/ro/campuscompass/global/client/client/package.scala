package ro.campuscompass.global.client

import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.Client
import org.typelevel.ci.CIString
import ro.campuscompass.global.domain.Node

package object client {
  def request[F[_], In](path: String, method: Method = Method.GET, entity: In = Entity.empty)(implicit
    encoder: EntityEncoder[F, In],
    apiKey: String
  ) = Request[F](
    method  = method,
    uri     = Uri.unsafeFromString(s"$path"),
    headers = Headers(Header.Raw(CIString("X-Regional-Api-Key"), apiKey))
  ).withEntity[In](entity)

  def expectResponse[F[_], Out](client: Client[F], request: Request[F])(implicit entityDecoder: EntityDecoder[F, Out]): F[Out] =
    client.expect[Out](request)
}
