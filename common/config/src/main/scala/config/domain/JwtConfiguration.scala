package config.domain

import pureconfig._
import pureconfig.generic.derivation.default._

case class JwtConfiguration (sha256Key:String) derives ConfigReader
