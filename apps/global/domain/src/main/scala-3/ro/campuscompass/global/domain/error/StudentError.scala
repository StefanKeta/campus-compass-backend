package ro.campuscompass.global.domain.error

enum StudentError extends Throwable:
  case AlreadyAppliedToUniversity(cause: String)
  case ApplicationNotFound(cause: String)
  case EmailAlreadyExists(cause: String)
  case NonExistingUniversity(cause: String)
  case ProgrammeNotFound(cause: String)
  case StudentDataExists(cause: String)
  case StudentDataDoesNotExist(cause: String)
