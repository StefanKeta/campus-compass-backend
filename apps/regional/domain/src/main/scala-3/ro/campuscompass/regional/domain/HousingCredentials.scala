package ro.campuscompass.regional.domain

import ro.campuscompass.common.domain.Credentials

import java.util.UUID

case class HousingCredentials(
  studentId: UUID,
  universityId: UUID,
  credentials: Credentials
)
