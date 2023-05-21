package ro.campuscompass.regional.domain

import java.util.UUID

final case class StudyProgram(
  _id: UUID,
  universityId: UUID,
  name: String,
  kind: String,
  language: String
)
