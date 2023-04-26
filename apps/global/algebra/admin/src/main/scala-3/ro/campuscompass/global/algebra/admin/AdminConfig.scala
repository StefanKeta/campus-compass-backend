package ro.campuscompass.global.algebra.admin

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class AdminConfig(username: String, password: String) derives ConfigReader
