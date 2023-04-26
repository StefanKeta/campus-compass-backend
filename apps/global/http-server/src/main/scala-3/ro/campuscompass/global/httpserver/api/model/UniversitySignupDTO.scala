package ro.campuscompass.global.httpserver.api.model

import ro.campuscompass.global.domain.Coordinates

final case class UniversitySignupDTO(
  name: String,
  contactPerson: String,
  email: String,
  coordinates: Coordinates
)
