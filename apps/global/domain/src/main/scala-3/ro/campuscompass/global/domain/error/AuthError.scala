package ro.campuscompass.global.domain.error

enum AuthError extends Throwable:
  case WrongCredentials(cause: String)
