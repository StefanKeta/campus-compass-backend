package ro.campuscompass.common.domain

import java.time.Instant
import java.util.UUID

final case class StudentApplicationDTO(
  applicationId: UUID,
  timestamp: Instant,
  universityId: UUID,
  programName: String,
  programKind: String,
  programLanguage: String
)
