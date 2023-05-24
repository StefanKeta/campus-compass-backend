package ro.campuscompass.regional.httpserver.api.model

import ro.campuscompass.common.domain.StudyProgram
import java.util.UUID

final case class CreateStudyProgramDTO(name: String, kind: String, language: String) {
  def domain(_id: UUID, universityId: UUID): StudyProgram = StudyProgram(
    _id          = _id,
    universityId = universityId,
    name         = name,
    kind         = kind,
    language     = language
  )
}
