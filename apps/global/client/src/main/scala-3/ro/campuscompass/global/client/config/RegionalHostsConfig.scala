package ro.campuscompass.global.client.config

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class RegionalHostsConfig(studentBE: String, universityBE: String, regionalFE: String) derives ConfigReader
