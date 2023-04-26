package ro.campuscompass.global.domain

import io.circe.generic.auto.*

import java.util.UUID

final case class University(_id: UUID, name: String, contactPerson: String, email: String, coordinates: Coordinates)
