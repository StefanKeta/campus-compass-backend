package ro.campuscompass.common.minio

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class MinIOConfig(endpoint: String, accessKey: String, secretAccessKey: String) derives ConfigReader
