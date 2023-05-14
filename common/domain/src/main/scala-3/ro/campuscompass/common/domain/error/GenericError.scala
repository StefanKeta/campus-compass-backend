package ro.campuscompass.common.domain.error

final case class GenericError (message: String) extends Throwable

object GenericError {
  def apply(e: Throwable): GenericError = new GenericError(e.getMessage)
}
