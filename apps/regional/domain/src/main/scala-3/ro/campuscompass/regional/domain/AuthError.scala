package ro.campuscompass.regional.domain

enum AuthError extends Throwable:
  case AuthUniversityError(cause: String)
  case AuthStudentError(cause:String)