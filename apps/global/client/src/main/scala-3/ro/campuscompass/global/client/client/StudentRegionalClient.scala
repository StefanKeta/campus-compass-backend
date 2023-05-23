package ro.campuscompass.global.client.client

import cats.Applicative
import cats.effect.kernel.{ Async, Concurrent }
import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.typelevel.ci.CIString
import ro.campuscompass.common.crypto.JWT
import ro.campuscompass.global.client.*
import ro.campuscompass.global.client.api.model.response.{ AppliedProgramme, UniversityProgramme, ViewApplicationRedirectDTO }
import ro.campuscompass.global.client.api.model.request.{ ListAppliedProgrammesReqDTO, ViewApplicationReqDTO }
import ro.campuscompass.global.client.config.{ ApiKeyConfig, RegionalConfig }
import ro.campuscompass.global.domain.{ Node, StudentApplication }
import sttp.tapir.*

import java.util.UUID

trait StudentRegionalClient[F[_]] {
  def applyToProgramme(studentApplication: StudentApplication, node: Node): F[Option[UUID]]
  def listProgrammes(): F[List[UniversityProgramme]]
  def listAppliedProgrammes(studentId: UUID): F[List[AppliedProgramme]]
  def viewApplication(viewApplication: ViewApplicationReqDTO, node: Node): F[ViewApplicationRedirectDTO]
}

object StudentRegionalClient {
  def apply[F[_]: Async](
    client: Client[F],
    regionalConfig: RegionalConfig,
    apiKeyConfig: ApiKeyConfig
  ) =
    new StudentRegionalClient[F]:
      implicit val key: String = apiKeyConfig.key

      override def applyToProgramme(studentApplication: StudentApplication, node: Node): F[Option[UUID]] =
        expectResponse[F, Option[UUID]](
          client,
          request[F, StudentApplication](
            s"${node.be}/api/v1/student/apply/${studentApplication.programmeId}",
            entity = studentApplication
          ),
        )

      override def listProgrammes(): F[List[UniversityProgramme]] = regionalConfig.nodes.map { node =>
        expectResponse[F, List[UniversityProgramme]](
          client,
          request[F, Unit](s"${node.be}/api/v1/student/apply/programmes")
        )
      }.sequence.map(_.flatten)

      override def listAppliedProgrammes(studentId: UUID): F[List[AppliedProgramme]] = regionalConfig.nodes.map { node =>
        expectResponse[F, List[AppliedProgramme]](
          client,
          request[F, ListAppliedProgrammesReqDTO](
            s"${node.be}/api/v1/student/applications/$studentId",
            entity = ListAppliedProgrammesReqDTO(studentId)
          )
        )
      }.sequence.map(_.flatten)

      override def viewApplication(viewApplication: ViewApplicationReqDTO, node: Node): F[ViewApplicationRedirectDTO] =
        for {
          jwt <- expectResponse[F, JWT](
            client,
            request[F, ViewApplicationReqDTO](
              s"${node.be}/api/v1/student/applications",
              entity = viewApplication
            )
          )
        } yield ViewApplicationRedirectDTO(jwt, node.fe)
}
