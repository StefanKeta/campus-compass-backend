package ro.campuscompass.global.client.api.model.response

import ro.campuscompass.global.domain.DegreeType

import java.util.UUID

final case class AppliedProgramme(uniUserId: UUID, applicationId: UUID, name: String, degreeType: DegreeType)
