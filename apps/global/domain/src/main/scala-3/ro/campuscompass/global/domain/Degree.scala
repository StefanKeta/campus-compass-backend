package ro.campuscompass.global.domain

import java.time.Instant

final case class Degree(
  typeOfDegree: Option[DegreeType],
  date: Option[Instant],
  country: Option[String]
)
