package ro.campuscompass.regional.domain

import ro.campuscompass.common.domain.Credentials

import java.util.UUID

final case class HousingCredentials(
  studentId: UUID,
  universityId: UUID,
  credentials: Credentials
)
