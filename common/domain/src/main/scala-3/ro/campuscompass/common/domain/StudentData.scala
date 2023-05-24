package ro.campuscompass.common.domain

import java.time.Instant
import java.util.UUID

final case class StudentData(
  _id: UUID,
  firstName: Option[String],
  lastName: Option[String],
  dob: Option[Instant],
  email: Option[String],
  phone: Option[String],
  language: Option[String],
  address: Option[Address],
  degree: Option[Degree]
)
