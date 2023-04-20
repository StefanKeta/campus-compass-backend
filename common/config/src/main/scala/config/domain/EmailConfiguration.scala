package config.domain

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default._

case class EmailConfiguration(
    sender: String,
    smtpUsername: String,
    smtpPassword: String,
    smtpHost: String,
    smtpPort: Int
) derives ConfigReader
