package ro.campuscompass.global.domain

import java.time.Instant

final case class Degree(
  typeOfDegree: DegreeType = DegreeType.Bachelor,
  date: Instant,
  country: String
)
