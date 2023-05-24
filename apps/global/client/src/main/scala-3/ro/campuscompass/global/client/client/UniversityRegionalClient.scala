package ro.campuscompass.global.client.client

import cats.Applicative
import cats.effect.kernel.Concurrent
import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.Client
import org.typelevel.ci.CIString
import ro.campuscompass.common.crypto.JWT
import ro.campuscompass.common.domain.{AuthorizeUniversityDTO, StudyProgramDTO}
import ro.campuscompass.global.client.api.model.request.UniversityLoginRequestDTO
import ro.campuscompass.global.client.client
import ro.campuscompass.global.client.config.*
import ro.campuscompass.global.domain.Node

import java.util.UUID

trait UniversityRegionalClient[F[_]] {
  def generateUniversityUserJwt(userId: UUID, node: Node): F[JWT]
  def listProgrammes(): F[List[StudyProgramDTO]]
}

object UniversityRegionalClient {
  def apply[F[_]: Concurrent](
    client: Client[F],
    regionalConfig: RegionalConfig,
    apiKeyConfig: ApiKeyConfig
  ) =
    new UniversityRegionalClient[F] {
      implicit val key: String = apiKeyConfig.key
      override def generateUniversityUserJwt(userId: UUID, node: Node): F[JWT] = for {
        request <- Applicative[F].pure(request[F, AuthorizeUniversityDTO](
          s"http://${node.be}/api/v1/authorize/university",
          method = Method.POST,
          entity = AuthorizeUniversityDTO(userId)
        ))
        jwt <- expectResponse[F, JWT](client, request)
      } yield jwt

      override def listProgrammes(): F[List[StudyProgramDTO]] = regionalConfig.nodes.map(node =>
        val req = request[F, Unit](path = s"http://${node.be}/api/v1/program")
        expectResponse[F, List[StudyProgramDTO]](client, req)
      ).sequence.map(_.flatten)
    }
}