package ro.campuscompass.global.domain

import io.circe.generic.auto.*

import java.util.UUID

final case class ProgrammeApplication(
  studentId: UUID,
  programmeId: UUID,
  universityUserId: UUID
)
