package ro.campuscompass.global.domain

import java.util.UUID

final case class UniversityProgrammeGlobal(
  uniUserId: UUID,
  programmeName: String,
  degreeType: String,
  universityName: String
)
