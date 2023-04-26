package ro.campuscompass.global.domain.error

enum AdminError extends Throwable:
  case Unauthorized(cause: String)
  case UniversityNotFound(cause: String)
  case UniversityAlreadyConfirmed(cause: String)
