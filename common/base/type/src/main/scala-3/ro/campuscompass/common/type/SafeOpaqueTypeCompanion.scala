package ro.campuscompass.common.`type`

trait SafeOpaqueTypeCompanion[E, W <: T, T] extends OpaqueTypeInstances[W, T] {

  def apply(t: T): Either[E, W]

  def unsafe(t: T): W = t.asInstanceOf[W]

  extension (w: W) {
    def value: T = w.asInstanceOf[T]
  }
}
