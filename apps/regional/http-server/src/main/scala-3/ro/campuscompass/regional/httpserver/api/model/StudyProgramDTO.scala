package ro.campuscompass.regional.httpserver.api.model

import ro.campuscompass.regional.domain.StudyProgram

import java.util.UUID

final case class StudyProgramDTO(_id: UUID, universityId: UUID, name: String, kind: String, language: String)

object StudyProgramDTO {
  def apply(studyProgram: StudyProgram) =
    new StudyProgramDTO(
      _id          = studyProgram._id,
      universityId = studyProgram.universityId,
      name         = studyProgram.name,
      kind         = studyProgram.kind,
      language     = studyProgram.language
    )
}
