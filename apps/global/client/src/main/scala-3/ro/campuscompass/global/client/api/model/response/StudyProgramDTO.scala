package ro.campuscompass.global.client.api.model.response

import java.util.UUID

final case class StudyProgramDTO(_id: UUID, universityId: UUID, name: String, kind: String, language: String)
