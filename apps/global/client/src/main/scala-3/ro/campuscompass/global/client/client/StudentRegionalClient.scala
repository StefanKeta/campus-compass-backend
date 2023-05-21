package ro.campuscompass.global.client.client

import cats.Applicative
import cats.effect.kernel.{Async, Concurrent}
import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.typelevel.ci.CIString
import ro.campuscompass.global.client.*
import ro.campuscompass.global.client.api.model.response.{AppliedProgramme, UniversityProgramme, ViewApplicationRedirectDTO}
import ro.campuscompass.global.client.api.model.request.{ListAppliedProgrammesReqDTO, ViewApplicationReqDTO}
import ro.campuscompass.global.client.config.{ApiKeyConfig, RegionalConfig, RegionalHostsConfig}
import ro.campuscompass.global.domain.{Node, StudentApplication}
import sttp.tapir.*

import java.util.UUID

trait StudentRegionalClient[F[_]] {
  def applyToProgramme(studentApplication: StudentApplication, node: Node): F[Option[UUID]]
  def listProgrammes(): F[List[UniversityProgramme]]
  def listAppliedProgrammes(studentId: UUID): F[List[AppliedProgramme]]
  def viewApplication(viewApplication: ViewApplicationReqDTO, node: Node): F[Option[ViewApplicationRedirectDTO]]
}

object StudentRegionalClient {
  def apply[F[_]: Async](
    client: Client[F],
    regionalConfig: RegionalConfig,
    hostsConfig: RegionalHostsConfig,
    apiKeyConfig: ApiKeyConfig
  ) =
    new StudentRegionalClient[F]:
      implicit val key: String = apiKeyConfig.key

      override def applyToProgramme(studentApplication: StudentApplication, node: Node): F[Option[UUID]] =
        expectResponse[F, Option[UUID]](
          client,
          request[F, StudentApplication](
            s"${node.host}.${hostsConfig.studentBE}/student/apply/${studentApplication.programmeId}",
            entity = studentApplication
          ),
        )

      override def listProgrammes(): F[List[UniversityProgramme]] = regionalConfig.nodes.map { node =>
        expectResponse[F, List[UniversityProgramme]](
          client,
          request[F, Unit](s"${node.host}.${hostsConfig.studentBE}/student/apply/programmes")
        )
      }.sequence.map(_.flatten)

      override def listAppliedProgrammes(studentId: UUID): F[List[AppliedProgramme]] = regionalConfig.nodes.map { node =>
        expectResponse[F, List[AppliedProgramme]](
          client,
          request[F, ListAppliedProgrammesReqDTO](s"${node.host}.${hostsConfig.studentBE}/student/applications/$studentId", entity = ListAppliedProgrammesReqDTO(studentId))
        )
      }.sequence.map(_.flatten)

      override def viewApplication(viewApplication: ViewApplicationReqDTO, node: Node): F[Option[ViewApplicationRedirectDTO]] =
        expectResponse[F, Option[ViewApplicationRedirectDTO]](
          client,
          request[F, ViewApplicationReqDTO](
            s"${node.host}.${hostsConfig.studentBE}/student/applications/",
            entity = viewApplication
          )
        )

//      private def requestForNode(path: String, node: Node) = Request[F](
//        method  = Method.GET,
//        uri     = Uri.unsafeFromString(s"${node.host}.${hostsConfig.studentBE}/"),
//        headers = Headers(Header.Raw(CIString("X-Regional-Api-Key"), apiKeyConfig.key))
//      )
//
//      private def expectResponse[In, Out](path: String, node: Node)(
//        method: Method = Method.GET,
//        entity: In     = Entity.empty
//      )(implicit entityDecoder: EntityDecoder[F, Out]): F[Out] =
//        client.expect[Out](requestForNode(path, node))
}
