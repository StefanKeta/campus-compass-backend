package ro.campuscompass.global.client.api.model.response

import java.util.UUID

final case class UniversityProgramme(uniUserId: UUID, programmeName: String, typeOfProgramme: String)
