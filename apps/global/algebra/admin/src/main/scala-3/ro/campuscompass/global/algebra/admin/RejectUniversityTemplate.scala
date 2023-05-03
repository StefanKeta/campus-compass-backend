package ro.campuscompass.global.algebra.admin

import cats.effect.Async
import cats.implicits.*
import ro.campuscompass.common.effect.FileUtils

class RejectUniversityTemplate(template:String) {
  def apply(sender: String): String =
    template
      .replace("$sender", sender)
}

object RejectUniversityTemplate {
  def apply[F[_] : Async]: F[RejectUniversityTemplate] =
    FileUtils.readTextFromResource("reject-university-email-template.html")
      .map(new RejectUniversityTemplate(_))
}
