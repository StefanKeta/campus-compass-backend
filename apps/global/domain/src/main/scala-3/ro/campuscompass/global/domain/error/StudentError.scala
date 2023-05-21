package ro.campuscompass.global.domain.error

enum StudentError extends Throwable:
  case NonExistingUniversity(cause: String)
  case EmailAlreadyExists(cause: String)
  case AlreadyAppliedToUniversity(cause: String)
  case ProgrammeNotFound(cause: String)
  case ApplicationNotFound(cause: String)
