package error

enum AuthError extends Throwable:
  case WrongCredentials(cause:String)
