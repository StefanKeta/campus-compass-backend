package ro.campuscompass.common.email

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class EmailConfig(
  sender: String,
  smtpUsername: String,
  smtpPassword: String,
  smtpHost: String,
  smtpPort: Int
) derives ConfigReader
