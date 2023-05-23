package ro.campuscompass.global.domain

import java.util.UUID

final case class AppliedProgrammeGlobal(
  uniUserId: UUID,
  applicationId: UUID,
  name: String,
  degreeType: DegreeType,
  universityName: String
)
