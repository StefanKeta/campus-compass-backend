package ro.campuscompass.common.domain

import java.time.Instant

final case class Degree(
  typeOfDegree: Option[String],
  date: Option[Instant],
  country: Option[String]
)
