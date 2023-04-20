package dao

import domain.user.{Entity, Role}
import io.circe.generic.auto.*
import mongo4cats.circe.*
import mongo4cats.codecs.MongoCodecProvider

import java.util.UUID

case class UserDAO (_id:UUID, username:String, password:String, role:Role)

object UserDAO{
  given userProvider: MongoCodecProvider[UserDAO] = deriveCirceCodecProvider[UserDAO]
}
