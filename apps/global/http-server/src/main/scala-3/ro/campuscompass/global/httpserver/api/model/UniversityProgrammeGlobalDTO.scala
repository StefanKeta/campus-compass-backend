package ro.campuscompass.global.httpserver.api.model

import ro.campuscompass.global.domain.UniversityProgrammeGlobal

import java.util.UUID
import scala.annotation.tailrec

final case class UniversityProgrammeGlobalDTO(
  uniUserId: UUID,
  programmeName: String,
  programmeId: UUID,
  degreeType: String,
  universityName: String
)

object UniversityProgrammeGlobalDTO {
  def apply(universityProgrammeGlobal: UniversityProgrammeGlobal): UniversityProgrammeGlobalDTO = UniversityProgrammeGlobalDTO(
    uniUserId      = universityProgrammeGlobal.uniUserId,
    programmeName  = universityProgrammeGlobal.programmeName,
    programmeId    = universityProgrammeGlobal.programmeId,
    degreeType     = universityProgrammeGlobal.degreeType,
    universityName = universityProgrammeGlobal.universityName
  )
}
