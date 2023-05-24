package ro.campuscompass.common.domain

import java.util.UUID

final case class AuthorizeStudentDTO(studentId: UUID, applicationId: UUID)
