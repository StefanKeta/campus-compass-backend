package endpoints

import domain.Entity
import io.circe.generic.auto.*
import mongo4cats.bson.ObjectId
import mongo4cats.circe.*
import mongo4cats.codecs.MongoCodecProvider
import sttp.tapir.Schema
import sttp.tapir.Schema.schemaForString
import sttp.tapir.generic.auto.*

package object admin {

  given providedCodec: MongoCodecProvider[Entity] = deriveCirceCodecProvider[Entity]
  given objectIdSchema: Schema[ObjectId] = Schema.schemaForString.map(id => Some(ObjectId(id)))(_.toString)
}
