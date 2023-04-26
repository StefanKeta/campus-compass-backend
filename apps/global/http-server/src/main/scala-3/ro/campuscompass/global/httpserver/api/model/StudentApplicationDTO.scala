package ro.campuscompass.global.httpserver.api.model

import java.util.UUID

final case class StudentApplicationDTO(firstName: String, lastName: String, email: String, appliedTo: UUID)
