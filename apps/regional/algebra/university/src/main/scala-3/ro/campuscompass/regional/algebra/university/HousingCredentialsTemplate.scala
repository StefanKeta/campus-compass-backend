package ro.campuscompass.regional.algebra.university

import cats.effect.*
import cats.implicits.*
import ro.campuscompass.common.effect.FileUtils

class HousingCredentialsTemplate(template: String) {
  def apply(username: String, password: String, sender: String): String =
    template
      .replace("$username", username)
      .replace("$password", password)
      .replace("$sender", sender)
}

object HousingCredentialsTemplate {
  def apply[F[_]: Async]: F[HousingCredentialsTemplate] =
    FileUtils.readTextFromFile("housing-credentials-template.html")
      .map(new HousingCredentialsTemplate(_))
}
