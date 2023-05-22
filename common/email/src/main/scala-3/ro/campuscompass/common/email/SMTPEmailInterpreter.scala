package ro.campuscompass.common.email

import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*
import retry.implicits.*
import retry.{RetryPolicies, RetryPolicy}
import ro.campuscompass.common.logging.*

import javax.mail.*
import javax.mail.internet.*
import scala.concurrent.duration.DurationInt

class SMTPEmailInterpreter[F[_]: Async](emailConfig: EmailConfig, session: Session) extends EmailAlgebra[F] with Logging {

  private lazy val retryPolicy: RetryPolicy[F] = RetryPolicies.exponentialBackoff[F](2.seconds)
    .join(RetryPolicies.limitRetries[F](3))

  override def config: EmailConfig = emailConfig

  override def send(request: EmailRequest): F[Unit] =
    MonadCancel[F].guaranteeCase {

      Sync[F].interruptibleMany {
        val message           = new MimeMessage(session)
        val recipientsAddress = request.to.map(email => new InternetAddress(email.value))

        val mimeBodyPart = new MimeBodyPart
        mimeBodyPart.setContent(request.content, "text/html; charset=UTF-8")

        val mimeMultipart = new MimeMultipart
        mimeMultipart.addBodyPart(mimeBodyPart)

        message.setFrom(new InternetAddress(request.from.value))
        message.setRecipients(Message.RecipientType.TO, recipientsAddress.toArray[Address])
        message.setSubject(request.subject.value)
        message.setContent(mimeMultipart)

        Transport.send(message)
      }.retryingOnAllErrors(
        policy = retryPolicy,
        onError = {
          case (e, retryDetails) => logger.info(s"[RETRY] send email to ${request.to} - $retryDetails", e)
        }
      )
    } {
      case Outcome.Succeeded(_) => logger.info(s"[SUCCESS] send email to ${request.to}")
      case Outcome.Errored(e)   => logger.warn(s"[ERROR] send email to ${request.to}", e)
      case Outcome.Canceled()   => logger.info(s"[CANCEL] send email to ${request.to}")
    }
}

object SMTPEmailInterpreter extends Logging {

  def apply[F[_]: Async](emailConfig: EmailConfig): F[EmailAlgebra[F]] = {
    def configureSession: F[Session] =
      Sync[F].delay {
        val systemProperties = System.getProperties
        systemProperties.setProperty("mail.smtp.from", emailConfig.sender)
        systemProperties.put("mail.smtp.host", emailConfig.smtpHost)
        systemProperties.put("mail.smtp.port", emailConfig.smtpPort)
        systemProperties.put("mail.smtp.password", emailConfig.smtpPassword)
        systemProperties.put("mail.smtp.user", emailConfig.smtpUsername)
        systemProperties.put("mail.smtp.starttls.enable", "true")
        systemProperties.put("mail.smtp.ssl.enable", "true")
        systemProperties.put("mail.smtp.auth", "true")
        systemProperties.put("mail.transport.protocol", "smtps")
        Session.getInstance(
          systemProperties,
          new Authenticator {
            override def getPasswordAuthentication: PasswordAuthentication =
              new PasswordAuthentication(emailConfig.smtpUsername, emailConfig.smtpPassword)
          }
        )
      }

    for {
      session <- configureSession
    } yield new SMTPEmailInterpreter[F](emailConfig, session)
  }

}
