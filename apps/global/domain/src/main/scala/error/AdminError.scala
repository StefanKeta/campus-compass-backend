package error

enum AdminError extends Throwable:
  case Unauthorized(caused:String)