package ro.campuscompass.regional.domain

import ro.campuscompass.common.domain.StudentData

import java.time.Instant
import java.util.UUID
import io.circe.{ Codec, Decoder, Encoder }
import io.circe.derivation.Default.*

// TOO lazy to define all data types... we'll use only this one
final case class Application(
  _id: UUID,
  studentId: UUID,
  programId: UUID,
  programName: String,
  zipFile: Option[String],
  status: ApplicationStatus,
  housing: Boolean,
  sentHousingCredentials: Option[Boolean],
  timestamp: Instant,
  studentData: StudentData
)

enum ApplicationStatus:
  case InProcess
  case Submitted
  case Accepted
  case Rejected

object ApplicationStatus {
  given Encoder[ApplicationStatus] = Encoder[String].contramap(_.toString)
  given Decoder[ApplicationStatus] = Decoder[String].emap {
    case "InProcess" => Right(ApplicationStatus.InProcess)
    case "Submitted" => Right(ApplicationStatus.InProcess)
    case "Accepted"  => Right(ApplicationStatus.InProcess)
    case "Rejected"  => Right(ApplicationStatus.InProcess)
    case _           => Left("Could not parse the Application Status")
  }
}
