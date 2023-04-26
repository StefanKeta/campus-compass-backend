package ro.campuscompass.common.effect

import cats.effect.*

import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, FileVisitor, Files, Path}

object FileUtils {
  /**
   * Reads a whole text file in memory.
   */
  def readTextFromFile[F[_]: Async](path: String): F[String] =
    readTextFromFile(new File(path))

  /**
   * Reads a whole text file in memory.
   */
  def readTextFromFile[F[_]: Async](file: File): F[String] =
    fs2.io.file.Files[F]
      .readUtf8(fs2.io.file.Path.fromNioPath(file.toPath))
      .compile
      .string

  /**
   * Reads text from resource.
   */
  def readTextFromResource[F[_]: Async](resourceName: String, loader: Option[ClassLoader] = None): F[String] =
    readTextFromFile(loader.getOrElse(getClass.getClassLoader).getResource(resourceName).getPath)

  def writeTextToFile[F[_]: Async](file: File, content: String): F[Unit] =
    fs2.Stream.emits(content.getBytes)
      .through(fs2.io.file.Files[F].writeAll(fs2.io.file.Path.fromNioPath(file.toPath)))
      .compile
      .drain

}
