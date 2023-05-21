package ro.campuscompass.global.httpserver.api.model

import ro.campuscompass.global.domain.StudentApplication

import java.util.UUID

final case class StudentApplicationDTO(
  firstName: String,
  lastName: String,
  email: String,
  programmeId: UUID,
  universityUserId: UUID
) {
  def domain(_id: UUID, userId: UUID) = StudentApplication(
    _id              = _id,
    userId           = userId,
    firstName        = firstName,
    lastName         = lastName,
    email            = email,
    programmeId      = programmeId,
    universityUserId = universityUserId
  )
}
