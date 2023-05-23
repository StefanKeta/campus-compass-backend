package ro.campuscompass.global.persistence.rep

import io.circe.generic.auto.*
import mongo4cats.circe.*
import mongo4cats.codecs.MongoCodecProvider
import ro.campuscompass.global.domain.{ Address, Degree, StudentData }

import java.time.Instant
import java.util.UUID

final case class StudentDataRep(
  _id: UUID,
  firstName: Option[String],
  lastName: Option[String],
  dob: Option[Instant],
  email: Option[String],
  phone: Option[String],
  language: Option[String],
  address: Option[Address],
  degree: Option[Degree]
) {
  def domain() = StudentData(
    firstName = this.firstName,
    lastName  = this.lastName,
    dob       = this.dob,
    email     = this.email,
    phone     = this.phone,
    language  = this.language,
    address   = this.address,
    degree    = this.degree
  )
}

object StudentDataRep {
  given MongoCodecProvider[StudentDataRep] = deriveCirceCodecProvider[StudentDataRep]
  def apply(studentId: UUID, studentData: StudentData): StudentDataRep = StudentDataRep(
    _id       = studentId,
    firstName = studentData.firstName,
    lastName  = studentData.lastName,
    dob       = studentData.dob,
    email     = studentData.email,
    phone     = studentData.phone,
    language  = studentData.language,
    address   = studentData.address,
    degree    = studentData.degree
  )
}
