package ro.campuscompass.common.mongo

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class MongoDBConfig(
  host: String,
  port: Int,
  user: String,
  password: String,
  database: String
) derives ConfigReader
