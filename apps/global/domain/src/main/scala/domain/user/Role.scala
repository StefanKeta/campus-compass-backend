package domain.user

import io.circe.{Decoder, DecodingFailure, Encoder, Json}

enum Role:
  case Admin
  case University
  case Student

object Role:
  given Encoder[Role] = role => Json.fromString(role.toString)
  given Decoder[Role] = cursor =>
    cursor.as[String].flatMap {
      case "Admin" => Right(Role.Admin)
      case "University" => Right(Role.University)
      case "Student" => Right(Role.Student)
      case other => Left(DecodingFailure(s"Unknown enum value: $other", cursor.history))
    }