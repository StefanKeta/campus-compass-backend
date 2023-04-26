package ro.campuscompass.common.logging

import sourcecode.*

final case class Source(
  file: String,
  line: Int
)

object Source {
  given fromContext(using f: File, l: Line): Source =
    Source(
      file = extractFile(f.value),
      line = l.value,
    )

  private def extractFile(file: String) = {
    val ExtractFile = """^.*?/src/(?:main|test)/scala/([^$]+\.scala)$""".r

    file match {
      case ExtractFile(relative) =>
        relative
      case _ =>
        file
    }
  }
}
