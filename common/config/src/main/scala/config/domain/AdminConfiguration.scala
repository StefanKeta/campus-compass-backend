package config.domain

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default._

case class AdminConfiguration(username: String, password: String) derives ConfigReader
