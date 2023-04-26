package ro.campuscompass.global.httpserver.api.model

import ro.campuscompass.global.domain.Coordinates

import java.util.UUID

final case class UniversityAdminDTO(_id: UUID, name: String, contactPerson: String, email: String, coordinates: Coordinates)
