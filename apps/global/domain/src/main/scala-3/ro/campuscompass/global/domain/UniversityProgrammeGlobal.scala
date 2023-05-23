package ro.campuscompass.global.domain

import java.util.UUID

case class UniversityProgrammeGlobal(
  uniUserId: UUID,
  programmeName: String,
  degreeType: DegreeType,
  universityName: String
)
