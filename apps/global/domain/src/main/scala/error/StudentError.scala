package error

enum StudentError extends Throwable:
  case NonExistingUniversity(cause:String)
  case EmailAlreadyEnrolled(cause:String)
  case AlreadyAppliedToUniversity(cause:String)