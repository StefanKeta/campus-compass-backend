package ro.campuscompass.global.persistence.rep

import io.circe.generic.auto.*
import mongo4cats.circe.*
import mongo4cats.codecs.MongoCodecProvider
import ro.campuscompass.global.domain.StudentApplication

import java.util.UUID

final case class StudentApplicationRep(
  _id: UUID,
  userId: UUID,
  universityId: UUID,
  firstName: String,
  lastName: String,
  email: String
) {
  def domain: StudentApplication = StudentApplication(
    _id          = _id,
    userId       = userId,
    universityId = universityId,
    firstName    = firstName,
    lastName     = lastName,
    email        = email
  )
}
object StudentApplicationRep {
  given MongoCodecProvider[StudentApplicationRep] = deriveCirceCodecProvider

  def apply(application: StudentApplication): StudentApplicationRep =
    StudentApplicationRep(
      application._id,
      application.userId,
      application.universityId,
      application.firstName,
      application.lastName,
      application.email
    )
}
