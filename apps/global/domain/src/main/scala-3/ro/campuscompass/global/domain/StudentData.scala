package ro.campuscompass.global.domain

import java.time.Instant
import java.util.UUID

final case class StudentData(
  firstName: Option[String],
  lastName: Option[String],
  dob: Option[Instant],
  email: Option[String],
  phone: Option[String],
  language: Option[String],
  address: Option[Address],
  degree: Option[Degree]
)
