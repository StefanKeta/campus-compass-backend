package ro.campuscompass.global.persistence.rep

import io.circe.generic.auto.*
import mongo4cats.circe.*
import mongo4cats.codecs.MongoCodecProvider
import ro.campuscompass.common.domain.Role
import ro.campuscompass.global.domain.User

import java.util.UUID

final case class UserRep(_id: UUID, username: String, password: String, role: Role) {
  def domain: User = User(_id, username, password, role)
}

object UserRep {
  given userProvider: MongoCodecProvider[UserRep] = deriveCirceCodecProvider[UserRep]

  def apply(user: User): UserRep = UserRep(
    user._id,
    user.username,
    user.password,
    user.role
  )
}
