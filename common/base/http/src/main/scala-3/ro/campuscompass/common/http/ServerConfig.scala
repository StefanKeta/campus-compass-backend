package ro.campuscompass.common.http

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class ServerConfig(host: String, port: Int) derives ConfigReader
