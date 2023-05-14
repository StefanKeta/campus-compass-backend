package ro.campuscompass.regional.domain

import java.time.Instant
import java.util.UUID

// TOO lazy to define all data types... we'll use only this one
final case class Application(
  _id: UUID,
  studentId: UUID,
  programId: UUID,
  firstName: String,
  lastName: String,
  email: String,
  previousEducation: String,
  phone: String,
  zipFile: Option[String],
  status: ApplicationStatus,
  housing: Boolean,
  sentHousingCredentials: Option[Boolean],
  timestamp: Instant
)

enum ApplicationStatus:
  case InProcess
  case Submitted
  case Accepted
  case Rejected
