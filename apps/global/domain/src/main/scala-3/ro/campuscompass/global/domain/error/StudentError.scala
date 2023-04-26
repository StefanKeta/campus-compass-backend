package ro.campuscompass.global.domain.error

enum StudentError extends Throwable:
  case NonExistingUniversity(cause: String)
  case EmailAlreadyEnrolled(cause: String)
  case AlreadyAppliedToUniversity(cause: String)
