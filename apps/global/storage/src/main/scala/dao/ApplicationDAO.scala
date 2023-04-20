package dao
import io.circe.generic.auto.*
import mongo4cats.circe.*
import mongo4cats.codecs.MongoCodecProvider

import java.util.UUID

case class ApplicationDAO(
    _id: UUID,
    universityId: UUID,
    firstName: String,
    lastName: String,
    email: String
)

object ApplicationDAO{
  given MongoCodecProvider[ApplicationDAO] = deriveCirceCodecProvider
}