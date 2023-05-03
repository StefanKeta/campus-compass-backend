package ro.campuscompass.global.domain

import io.circe.generic.auto.*
import ro.campuscompass.common.domain.Role

import java.time.Instant
import java.util.UUID

final case class User(_id: UUID, username: String, password: String, role: Role,registrationDate:Instant)
