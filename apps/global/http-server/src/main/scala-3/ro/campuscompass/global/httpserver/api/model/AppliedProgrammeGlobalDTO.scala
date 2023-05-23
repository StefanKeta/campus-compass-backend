package ro.campuscompass.global.httpserver.api.model

import ro.campuscompass.global.domain.{ AppliedProgrammeGlobal, DegreeType }

import java.util.UUID

case class AppliedProgrammeGlobalDTO(
  uniUserId: UUID,
  applicationId: UUID,
  name: String,
  degreeType: DegreeType,
  universityName: String
)

object AppliedProgrammeGlobalDTO {
  def apply(appliedProgrammeGlobal: AppliedProgrammeGlobal): AppliedProgrammeGlobalDTO = AppliedProgrammeGlobalDTO(
    uniUserId      = appliedProgrammeGlobal.uniUserId,
    applicationId  = appliedProgrammeGlobal.applicationId,
    name           = appliedProgrammeGlobal.name,
    degreeType     = appliedProgrammeGlobal.degreeType,
    universityName = appliedProgrammeGlobal.universityName
  )
}
