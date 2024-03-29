package ro.campuscompass.global.httpserver.api.model

import ro.campuscompass.global.domain.AppliedProgrammeGlobal

import java.util.UUID

final case class AppliedProgrammeGlobalDTO(
  uniUserId: UUID,
  applicationId: UUID,
  name: String,
  degreeType: String,
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
