package emailAlgebra

import cats.effect.kernel.Async
import cats.implicits.*
import config.domain.EmailConfiguration
import emailAlgebra.*
import emailAlgebra.impl.EmailAlgebraImpl
import org.typelevel.log4cats.Logger
import template.Template

import javax.mail.{Authenticator, PasswordAuthentication, Session}

trait EmailAlgebra[F[_]]{
  def sendEmail(template:Template):F[EmailFiber[F]]
}

object EmailAlgebra{
  def apply[F[_]](emailConfiguration: EmailConfiguration)(using F:Async[F], logger: Logger[F]): F[EmailAlgebraImpl[F]] = for {
    session <- configureSession(emailConfiguration)
  } yield new EmailAlgebraImpl[F](emailConfiguration, session)

  private def configureSession[F[_]](emailConfig:EmailConfiguration)(using F: Async[F]): F[Session] = F.delay {
    val systemProperties = System.getProperties
    systemProperties.setProperty("mail.smtp.from", emailConfig.sender)
    systemProperties.put("mail.smtp.host", emailConfig.smtpHost)
    systemProperties.put("mail.smtp.port", emailConfig.smtpPort)
    systemProperties.put("mail.smtp.password", emailConfig.smtpPassword)
    systemProperties.put("mail.smtp.user", emailConfig.smtpUsername)
    systemProperties.put("mail.smtp.starttls.enable", "true")
    systemProperties.put("mail.smtp.ssl.enable", "true")
    systemProperties.put("mail.smtps.auth", "true")
    systemProperties.put("mail.transport.protocol", "smtps")
    Session.getInstance(
      systemProperties,
      new Authenticator {
        override def getPasswordAuthentication: PasswordAuthentication =
          new PasswordAuthentication(emailConfig.smtpUsername, emailConfig.smtpPassword)
      },
    )
  }
}