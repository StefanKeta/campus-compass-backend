package ro.campuscompass.global.algebra.admin

import cats.effect.*
import cats.implicits.*
import ro.campuscompass.common.effect.FileUtils

class ConfirmUniversityTemplate(template: String) {
  def apply(username: String, password: String, sender: String): String =
    template
      .replace("$username", username)
      .replace("$password", password)
      .replace("$sender", sender)
}

object ConfirmUniversityTemplate {
  def apply[F[_]: Async]: F[ConfirmUniversityTemplate] =
    FileUtils.readTextFromResource("confirm-university-email-template.html")
      .map(new ConfirmUniversityTemplate(_))
}
