package error

enum UniversityError extends Throwable:
  case SomeError(cause:String)