package ro.campuscompass.global.httpserver.api.model

import ro.campuscompass.global.domain.StudentApplication

import java.util.UUID

final case class StudentApplicationDTO(firstName: String, lastName: String, email: String, appliedTo: UUID) {
  def domain(_id: UUID, userId: UUID) = StudentApplication(
    _id          = _id,
    userId       = userId,
    universityId = appliedTo,
    firstName    = firstName,
    lastName     = lastName,
    email        = email
  )
}
