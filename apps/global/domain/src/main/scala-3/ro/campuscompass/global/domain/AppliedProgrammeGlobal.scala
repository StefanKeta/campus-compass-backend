package ro.campuscompass.global.domain

import java.util.UUID

case class AppliedProgrammeGlobal(
  uniUserId: UUID,
  applicationId: UUID,
  name: String,
  degreeType: DegreeType,
  universityName: String
)
