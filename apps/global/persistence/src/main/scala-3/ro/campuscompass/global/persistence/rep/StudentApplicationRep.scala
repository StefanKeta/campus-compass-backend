package ro.campuscompass.global.persistence.rep

import io.circe.generic.auto.*
import mongo4cats.circe.*
import mongo4cats.codecs.MongoCodecProvider
import ro.campuscompass.global.domain.StudentApplication

import java.util.UUID

final case class StudentApplicationRep(
  _id: UUID,
  userId: UUID,
  firstName: String,
  lastName: String,
  email: String,
  programmeId: UUID,
  universityUserId: UUID
) {
  def domain: StudentApplication = StudentApplication(
    _id              = _id,
    userId           = userId,
    firstName        = firstName,
    lastName         = lastName,
    email            = email,
    programmeId      = programmeId,
    universityUserId = universityUserId
  )
}
object StudentApplicationRep {
  given MongoCodecProvider[StudentApplicationRep] = deriveCirceCodecProvider

  def apply(application: StudentApplication): StudentApplicationRep =
    StudentApplicationRep(
      application._id,
      application.userId,
      application.firstName,
      application.lastName,
      application.email,
      application.programmeId,
      application.universityUserId
    )
}
