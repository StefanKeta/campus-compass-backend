package ro.campuscompass.global.domain.error

enum AuthError extends Throwable:
  case WrongCredentials(cause: String)
  case StudentAlreadyEnrolled(cause:String)
  case InvalidJwt(cause: String)
  case UnauthorizedRole(cause:String)
