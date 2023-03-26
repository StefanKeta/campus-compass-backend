package config.domain

import pureconfig._
import pureconfig.generic.derivation.default._

case class AppConfiguration (server: ServerConfiguration,db:DbConfiguration,admin: AdminConfiguration) derives ConfigReader
