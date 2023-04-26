package ro.campuscompass.global.persistence.rep

import io.circe.generic.auto.*
import mongo4cats.circe.*
import mongo4cats.codecs.MongoCodecProvider
import ro.campuscompass.global.domain.{Coordinates, University}

import java.util.UUID

final case class UniversityRep(
  _id: UUID,
  userId: Option[UUID],
  name: String,
  contactPerson: String,
  email: String,
  coordinates: Coordinates
) {

  def domain: University =
    University(_id, name, contactPerson, email, coordinates)
}

object UniversityRep {
  given MongoCodecProvider[UniversityRep] = deriveCirceCodecProvider[UniversityRep]

  def apply(university: University): UniversityRep =
    UniversityRep(
      university._id,
      None,
      university.name,
      university.contactPerson,
      university.email,
      university.coordinates
    )

  def apply(university: University, userId: UUID): UniversityRep =
    apply(university).copy(userId = Some(userId))
}
