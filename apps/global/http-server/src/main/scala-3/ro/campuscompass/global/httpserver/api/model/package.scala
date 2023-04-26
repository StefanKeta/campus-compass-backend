package ro.campuscompass.global.httpserver.api.model

import ro.campuscompass.common.`type`.OpaqueTypeCompanion
import sttp.tapir.Codec
import sttp.tapir.CodecFormat.TextPlain

opaque type AuthToken = String

object AuthToken extends OpaqueTypeCompanion[AuthToken, String] {
  given Codec[String, AuthToken, TextPlain] = sttp.tapir.Codec.string
}
