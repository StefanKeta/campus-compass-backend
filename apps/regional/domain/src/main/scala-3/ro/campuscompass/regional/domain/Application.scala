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
  status: String,
  housing: Boolean,
  sentHousingCredentials: Option[Boolean],
  timestamp: Instant,
  studentData: StudentData
)
