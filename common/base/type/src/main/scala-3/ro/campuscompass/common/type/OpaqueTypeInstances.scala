package ro.campuscompass.common.`type`

import cats.{ Eq, Show }
import io.circe.Codec
import pureconfig.{ ConfigReader, ConfigWriter }

trait OpaqueTypeInstances[W <: T, T] {
  implicit def derive[G[_]](using g: G[T]): G[W] = g.asInstanceOf[G[W]]
}
