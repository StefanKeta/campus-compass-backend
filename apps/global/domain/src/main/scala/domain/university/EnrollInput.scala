package domain.university

import domain.Coordinates

case class EnrollInput(
    name: String,
    contactPerson: String,
    email: String,
    coordinates: Coordinates
)
