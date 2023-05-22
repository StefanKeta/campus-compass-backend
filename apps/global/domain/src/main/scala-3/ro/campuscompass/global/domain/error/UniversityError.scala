package ro.campuscompass.global.domain.error

enum UniversityError extends Throwable:
  case UniversityEnrolled(cause: String)
