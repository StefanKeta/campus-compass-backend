package ro.campuscompass.global.client.client

import cats.Applicative
import cats.effect.kernel.Concurrent
import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.Client
import org.typelevel.ci.CIString
import ro.campuscompass.common.crypto.JWT
import ro.campuscompass.global.client.api.model.request.UniversityLoginRequestDTO
import ro.campuscompass.global.domain.Node
import ro.campuscompass.global.client.client
import ro.campuscompass.global.client.api.model.response.StudyProgramDTO
import ro.campuscompass.global.client.config.{ ApiKeyConfig, RegionalConfig }

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
    new UniversityRegionalClient[F]:
      implicit val key: String = apiKeyConfig.key
      override def generateUniversityUserJwt(userId: UUID, node: Node): F[JWT] = for {
        request <- Applicative[F].pure(request[F, UUID](
          s"${node.be}/api/v1/authorize/university/$userId",
          method = Method.POST,
          entity = userId
        ))
        jwt <- expectResponse[F, JWT](client, request)
      } yield jwt

      override def listProgrammes(): F[List[StudyProgramDTO]] = regionalConfig.nodes.map(node =>
        val req = request[F, Unit](path = s"${node.be}/api/v1/program")
        expectResponse[F, List[StudyProgramDTO]](client, req)
      ).sequence.map(_.flatten)

////      private def request(host: String, uri: String, path: String) =
////        Request[F](method = Method.GET, Uri.unsafeFromString(s"$host.$uri/$path"))
//
//      def jwtRequest(universityLoginRequestDTO: UniversityLoginRequestDTO, node: Node) =
//        Request[F](
//          uri     = Uri.unsafeFromString(s"http://${node.host}.{${hostsConfig.universityBE}" + "authorize/university"),
//          headers = Headers(Header.Raw(CIString("X-Regional-Api-Key"), "apikey"))
//        ).withEntity(universityLoginRequestDTO)
}
