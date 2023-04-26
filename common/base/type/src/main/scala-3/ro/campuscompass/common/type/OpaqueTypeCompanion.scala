package ro.campuscompass.common.`type`

import scala.reflect.ClassTag

trait OpaqueTypeCompanion[W <: T, T] extends OpaqueTypeInstances[W, T] {

  def apply(t: T): W = t.asInstanceOf[W]

  extension (w: W) {
    def value: T = w.asInstanceOf[T]
  }
}
