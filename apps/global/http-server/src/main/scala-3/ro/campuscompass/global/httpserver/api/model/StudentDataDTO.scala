package ro.campuscompass.global.httpserver.api.model

import ro.campuscompass.common.domain.{Address, Degree, StudentData}
import java.time.Instant
import java.util.UUID

final case class StudentDataDTO(
  firstName: Option[String],
  lastName: Option[String],
  dob: Option[Instant],
  email: Option[String],
  phone: Option[String],
  language: Option[String],
  address: Option[Address],
  degree: Option[Degree]
) {
  def domain(_id: UUID): StudentData = StudentData(
    _id,
    firstName,
    lastName,
    dob,
    email,
    phone,
    language,
    address,
    degree
  )
}

object StudentDataDTO {
  def apply(studentData: StudentData): StudentDataDTO = StudentDataDTO(
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
