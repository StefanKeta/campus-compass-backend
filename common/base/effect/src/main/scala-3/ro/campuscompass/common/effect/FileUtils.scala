package ro.campuscompass.common.effect

import cats.effect.*

import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

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
    fs2.io.readInputStream[F](
      Sync[F].delay(loader.getOrElse(getClass.getClassLoader).getResourceAsStream(resourceName)),
      chunkSize = 1024
    )
      .through(fs2.text.utf8.decode)
      .compile
      .string

  def writeTextToFile[F[_]: Async](file: File, content: String): F[Unit] =
    fs2.Stream.emits(content.getBytes)
      .through(fs2.io.file.Files[F].writeAll(fs2.io.file.Path.fromNioPath(file.toPath)))
      .compile
      .drain

}
