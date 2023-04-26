package ro.campuscompass.global.domain

import io.circe.generic.auto.*

import java.util.UUID

final case class StudentApplication(
  _id: UUID,
  universityId: UUID,
  firstName: String,
  lastName: String,
  email: String
)
