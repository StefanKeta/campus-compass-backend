package ro.campuscompass.global.client.client

import cats.Applicative
import cats.effect.kernel.*
import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.typelevel.ci.CIString
import ro.campuscompass.common.crypto.JWT
import ro.campuscompass.common.domain.*
import ro.campuscompass.global.client.*
import ro.campuscompass.global.client.api.model.request.*
import ro.campuscompass.global.client.api.model.response.*
import ro.campuscompass.global.client.config.*
import ro.campuscompass.global.domain.*
import sttp.tapir.*

import java.util.UUID

trait StudentRegionalClient[F[_]] {
  def applyToProgramme(studentApplication: StudentApplication, studentData: StudentData, node: Node): F[Option[UUID]]
  def listProgrammes(): F[List[StudyProgramDTO]]
  def listAppliedProgrammes(studentId: UUID): F[List[AppliedProgramme]]
  def viewApplication(viewApplication: ViewApplicationReqDTO, node: Node): F[ViewApplicationRedirectDTO]
}

object StudentRegionalClient {
  def apply[F[_]: Async](
    client: Client[F],
    regionalConfig: RegionalConfig,
    apiKeyConfig: ApiKeyConfig
  ) =
    new StudentRegionalClient[F] {
      implicit val key: String = apiKeyConfig.key

      override def applyToProgramme(
        studentApplication: StudentApplication,
        studentData: StudentData,
        node: Node
      ): F[Option[UUID]] =
        expectResponse[F, Option[UUID]](
          client,
          request[F, CreateApplicationDTO](
            method = Method.POST,
            path   = s"http://${node.be}/api/v1/application",
            entity = CreateApplicationDTO(
              studentData = studentData,
              studentId   = studentApplication.userId,
              programId   = studentApplication.programmeId,
            )
          )
        )

      override def listProgrammes(): F[List[StudyProgramDTO]] = regionalConfig.nodes.map { node =>
        expectResponse[F, List[StudyProgramDTO]](
          client,
          request[F, Unit](s"http://${node.be}/api/v1/program")
        )
      }.sequence.map(_.flatten)

      override def listAppliedProgrammes(studentId: UUID): F[List[AppliedProgramme]] = regionalConfig.nodes.map { node =>
        expectResponse[F, List[AppliedProgramme]](
          client,
          request[F, ListAppliedProgrammesReqDTO](
            s"http://${node.be}/api/v1/$studentId/application",
            entity = ListAppliedProgrammesReqDTO(studentId)
          )
        )
      }.sequence.map(_.flatten)

      override def viewApplication(viewApplication: ViewApplicationReqDTO, node: Node): F[ViewApplicationRedirectDTO] =
        for {
          jwt <- expectResponse[F, JWT](
            client,
            request[F, ViewApplicationReqDTO](
              s"http://${node.be}/api/v1/authorize/student",
              entity = viewApplication
            )
          )
        } yield ViewApplicationRedirectDTO(jwt, node.fe)
    }
}
