package password

import cats.effect.Sync
import cats.effect.std.Random
import cats.implicits.*
import tsec.passwordhashers.*
import tsec.passwordhashers.jca.*

trait PasswordHasher[F[_]]{
  def generateRawPassword():F[String]
  def encryptPassword(password:String):F[String]
  def checkPassword(password:String,hash: String):F[Boolean]
}

object PasswordHasher{
  def apply[F[_]](using F:Sync[F],random: Random[F]) = new PasswordHasher[F]:
    override def generateRawPassword(): F[String] ={
      val lowercaseLetters = "abcdefghijklmnopqrstuvwxyz"
      val uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
      val numbers = "0123456789"
      val specialCharacters = "!@#$%^&*()_+-=[]{}|;':\"<>,.?/\\"

      val allChars = lowercaseLetters + uppercaseLetters + numbers + specialCharacters

      (1 to 12).map(_ => random.elementOf(allChars))
        .toList
        .sequence
        .map(_.mkString)
    }


    override def encryptPassword(password: String): F[String] = F.delay(SCrypt.hashpw(password))

    override def checkPassword(password: String,hash:String): F[Boolean] = SCrypt.checkpwBool[F](password,PasswordHash[SCrypt](hash))
}
