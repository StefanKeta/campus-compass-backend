package dao

import domain.Coordinates
import io.circe.generic.auto.*
import mongo4cats.circe.*
import mongo4cats.codecs.MongoCodecProvider
import java.util.UUID

case class UniversityDAO(_id:UUID, userId:UUID,name: String, contactPerson: String, email: String, coordinates: Coordinates)

object UniversityDAO{
  given MongoCodecProvider[UniversityDAO] = deriveCirceCodecProvider[UniversityDAO]
}
