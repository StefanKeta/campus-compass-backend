package ro.campuscompass.global.domain

import java.time.Instant
import java.util.UUID

final case class StudentData(
  firstName: String,
  lastName: String,
  dob: Instant,
  email: String,
  phone: String,
  language: String,
  address: Address,
  degree: Degree
)
