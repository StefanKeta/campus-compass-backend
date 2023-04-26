package ro.campuscompass.common.crypto

import cats.effect.Sync
import cats.effect.std.Random
import cats.implicits.*
import cats.{Applicative, MonadThrow}

import java.util.List

object PasswordGenerator {
  def generatePassword[F[_]: MonadThrow: Random](): F[String] = {
    val lowercaseLetters  = "abcdefghijklmnopqrstuvwxyz"
    val uppercaseLetters  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val numbers           = "0123456789"
    val specialCharacters = "!@#$%^&*()_+-=[]{}|;':\"<>,.?/\\"

    val allChars = lowercaseLetters + uppercaseLetters + numbers + specialCharacters

    (1 to 12).map(_ => Random[F].elementOf(allChars))
      .toList
      .sequence
      .map(_.mkString)
  }

  def generateToken[F[_]: Sync: Random](nBytes: Int): F[String] =
    fs2.Stream
      .evalSeq(Random[F]
        .nextBytes(nBytes).map(_.toSeq))
      .through(fs2.text.base64.encode[F])
      .compile
      .string
}
