package ro.campuscompass.regional.httpserver.api.model

import java.util.UUID

final case class AuthorizeStudentDTO(studentId: UUID, applicationId: UUID)
