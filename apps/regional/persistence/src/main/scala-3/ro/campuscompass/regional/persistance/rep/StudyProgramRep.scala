package ro.campuscompass.regional.persistance.rep

import io.circe.generic.auto.*
import mongo4cats.circe.*
import mongo4cats.codecs.MongoCodecProvider
import ro.campuscompass.common.domain.StudyProgram

import java.util.UUID
import java.util.UUID

final case class StudyProgramRep(_id: UUID, universityId: UUID, name: String, kind: String, language: String) {
  def domain: StudyProgram = StudyProgram(
    _id          = _id,
    universityId = universityId,
    name         = name,
    kind         = kind,
    language     = language
  )
}

object StudyProgramRep {
  given MongoCodecProvider[StudyProgramRep] = deriveCirceCodecProvider

  def apply(studyProgram: StudyProgram) =
    new StudyProgramRep(
      _id          = studyProgram._id,
      universityId = studyProgram.universityId,
      name         = studyProgram.name,
      kind         = studyProgram.kind,
      language     = studyProgram.language
    )
}
