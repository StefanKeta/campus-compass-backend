package ro.campuscompass.regional.httpserver.api.model

import java.time.Instant

final case class HousingRequestDTO(
  name: String,
  applicationDate: Instant,
  sentHousingCredentials: Option[Boolean]
)
