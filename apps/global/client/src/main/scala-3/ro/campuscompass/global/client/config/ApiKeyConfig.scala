package ro.campuscompass.global.client.config

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class ApiKeyConfig (key:String) derives ConfigReader
