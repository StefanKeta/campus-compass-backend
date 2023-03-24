package config.domain

import pureconfig._
import pureconfig.generic.derivation.default._

case class ServerConfiguration(host:String,port:Int) derives ConfigReader


