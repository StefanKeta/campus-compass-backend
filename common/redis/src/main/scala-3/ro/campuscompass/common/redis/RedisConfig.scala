package ro.campuscompass.common.redis

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class RedisConfig(redisUri: String) derives ConfigReader
