package ro.campuscompass.global.httpserver.api.model

import java.util.UUID

final case class ViewApplicationDTO(studentId: UUID, universityId: UUID, applicationId: UUID)
