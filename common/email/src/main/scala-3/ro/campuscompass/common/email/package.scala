package ro.campuscompass.common.email

import cats.implicits.*
import ro.campuscompass.common.`type`.*

import javax.mail.internet.InternetAddress

opaque type EmailAddress = String

object EmailAddress extends SafeOpaqueTypeCompanion[Throwable, EmailAddress, String] {

  def apply(address: String): Either[IllegalArgumentException, EmailAddress] =
    Either
      .catchNonFatal(new InternetAddress(address, true))
      .leftMap(error => IllegalArgumentException("Invalid email address (RFC822)", error))
      .flatMap(addr =>
        Either.cond(
          addr.getAddress.matches(".*@.*\\..*"),
          addr,
          IllegalArgumentException("Invalid email address (RFC822)")
        )
      )
      .map(addr => addr.getAddress)
}

opaque type Subject = String

object Subject extends OpaqueTypeCompanion[Subject, String]

opaque type Content = String

object Content extends OpaqueTypeCompanion[Content, String]

