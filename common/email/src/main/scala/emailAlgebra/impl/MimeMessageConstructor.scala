package emailAlgebra.impl

import cats.effect.Sync
import cats.implicits.*
import java.util.UUID
import javax.activation.DataHandler
import javax.mail.{Message, Session}
import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.util.ByteArrayDataSource


class MimeMessageConstructor[F[_]](implicit F: Sync[F]) {

  def constructMessage(session: Session, fromEmail: String)(
    toEmail: String,
    subject: String,
    body: String,
  )(implicit
    F: Sync[F]
                      ): F[MimeMessage] = for {
    messageHeaders <- configureMessageHeaders(session)(fromEmail, toEmail, subject)
    message <- configureMessageBodyAndAttachments(messageHeaders, body)
  } yield message

  private def configureMessageHeaders(
                                       session: Session
                                     )(from: String, to: String, subject: String): F[MimeMessage] = F.delay {
    val message = new MimeMessage(session)
    message.setFrom(new InternetAddress(from))
    message.setRecipient(Message.RecipientType.TO, new InternetAddress(to))
    message.setSubject(subject)
    message
  }

  private def configureMessageBodyAndAttachments(
                                                  message: MimeMessage,
                                                  body: String,
                                                ): F[MimeMessage] = for {
    mimeMultipart <- constructBody(body)
    _ <- F.delay(message.setContent(mimeMultipart))
  } yield message

  private def constructBody(body: String): F[MimeMultipart] = F.delay {
    val html = new MimeBodyPart
    val content = new MimeMultipart
    html.setContent(body, "text/html; charset=UTF-8")
    content.addBodyPart(html)
    content
  }
}

object MimeMessageConstructor {
  def apply[F[_]: Sync] = new MimeMessageConstructor[F]
}
