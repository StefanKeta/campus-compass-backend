package ro.campuscompass.common.crypto

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class JwtConfig(sha256Key: String) derives ConfigReader
