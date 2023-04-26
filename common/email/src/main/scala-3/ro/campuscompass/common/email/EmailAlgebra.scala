package ro.campuscompass.common.email

import cats.effect.*
import cats.implicits.*
import ro.campuscompass.common.logging.Logger

import javax.mail.{Authenticator, PasswordAuthentication, Session}

trait EmailAlgebra[F[_]] {
  def config: EmailConfig

  def send(request: EmailRequest): F[Unit]
}
