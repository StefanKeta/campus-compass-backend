package emailAlgebra.impl

import cats.effect.kernel.{Async, Outcome}
import cats.implicits.*
import config.domain.EmailConfiguration
import emailAlgebra.{EmailAlgebra, EmailFiber, EmailOutcome}
import org.typelevel.log4cats.Logger
import template.Template

import javax.mail.Session

class EmailAlgebraImpl[F[_]](emailConfiguration: EmailConfiguration,session: Session)(using F:Async[F],logger:Logger[F]) extends EmailAlgebra[F]{
  override def sendEmail(template: Template): F[EmailFiber[F]] = {
    lazy val messageToSend = for{
      transport <- F.blocking(session.getTransport())
      _ <- F.blocking(
        transport.connect(emailConfiguration.smtpHost,emailConfiguration.smtpUsername,emailConfiguration.smtpPassword)
      )
      message <- MimeMessageConstructor[F].constructMessage(session,emailConfiguration.sender)(
        toEmail = template.recipient,
        subject = template.subject,
        body = template.message
      )
      _ <- F.blocking(transport.sendMessage(message,message.getAllRecipients))
    }yield ()

    for {
      sendEmailFiber <- F.start(messageToSend)
      outcome <- sendEmailFiber.join
      _ <- outcome match
        case Outcome.Succeeded(_) => logger.info(s"Successfully sent email to ${template.recipient}")
        case Outcome.Errored(e) => logger.warn(e)(s"Failed to send email to ${template.recipient}")
        case Outcome.Canceled() => logger.info(s"Cancelled sending email to ${template.recipient}")
    }yield sendEmailFiber
  }
}
