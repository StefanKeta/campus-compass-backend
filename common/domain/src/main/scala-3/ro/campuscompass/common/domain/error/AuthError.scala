package ro.campuscompass.common.domain.error

enum AuthError extends Throwable:
  case WrongCredentials(cause: String)
  case StudentAlreadyEnrolled(cause:String)
  case InvalidJwt(cause: String)
  case Unauthorized(cause:String)
