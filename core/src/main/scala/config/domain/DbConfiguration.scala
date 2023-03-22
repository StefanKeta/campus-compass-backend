package config.domain

import pureconfig._
import pureconfig.generic.derivation.default._

case class DbConfiguration(
    host: String,
    port: Int,
    user: String,
    password: String,
    database: String
) derives ConfigReader
