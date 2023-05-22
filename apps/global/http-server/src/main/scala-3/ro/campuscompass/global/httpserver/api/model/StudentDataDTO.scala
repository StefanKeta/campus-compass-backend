package ro.campuscompass.global.httpserver.api.model

import ro.campuscompass.global.domain.{ Address, Degree, StudentData }

import java.time.Instant
import java.util.UUID

final case class StudentDataDTO(
  firstName: String,
  lastName: String,
  dob: Instant,
  email: String,
  phone: String,
  language: String,
  address: Address,
  degree: Degree
) {
  def domain(): StudentData = StudentData(
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
