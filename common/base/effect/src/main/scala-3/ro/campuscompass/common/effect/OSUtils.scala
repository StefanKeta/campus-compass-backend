package ro.campuscompass.common.effect

import cats.*
import cats.arrow.FunctionK
import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*
import cats.syntax.all.*
import org.apache.commons.text.StringEscapeUtils

import java.io.*
import java.nio.file.Path
import scala.jdk.FutureConverters.*

object OSUtils {

  /** Fetches the current user's HOME directory. */
  def userHome[F[_]: Sync]: F[Option[File]] =
    Sync[F].delay {
      Option(System.getProperty("user.home"))
        .filter(_.nonEmpty)
        .orElse(Option(System.getenv("HOME")))
        .filter(_.nonEmpty)
        .map(s => new File(s))
    }

  final case class CommandResult(
    exitCode: Int,
    stdout: String,
    stderr: String,
  )

  def executeCommand[F[_]: Async: NonEmptyParallel](command: Path, args: String*): F[CommandResult] =
    def readStream(in: InputStream): F[String] =
      fs2.io.readInputStream(Sync[F].pure(in), 4096)
        .through(fs2.text.utf8.decode)
        .compile
        .string

    Sync[F].blocking {
      @SuppressWarnings(Array("org.wartremover.warts.ToString"))
      val allArgs = command.toAbsolutePath.toString +: args

      Runtime.getRuntime.exec(allArgs.toArray)
    }.bracket { proc =>
      val stdout = proc.getInputStream
      val stderr = proc.getErrorStream

      val await = Sync[F].interruptible(proc.waitFor())

      (await, readStream(stdout), readStream(stderr))
        .parMapN { (code, stdout, stderr) =>
          CommandResult(code, stdout, stderr)
        }
    } { proc =>
      Async[F].fromFuture(Sync[F].delay(proc.destroyForcibly().onExit().asScala)).as(())
    }

  /**
   * Executes commands using the Unix Shell.
   *
   * Since arguments need proper exscaping, they should be specified using
   * the [[args]] parameter, rather then injecting them within the [[command]]
   * string. 
   */
  def executeShellCommand[F[_]: Async: NonEmptyParallel](command: String, args: String*): F[CommandResult] = {
    val allCommand = (command :: args.toList).map(StringEscapeUtils.escapeXSI).mkString(" ")
    executeCommand(Path.of("/bin/sh"), "-c", allCommand)
  }
}
