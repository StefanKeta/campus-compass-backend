package token

import cats.syntax.all.*
import cats.effect.Sync
import io.circe.Json
import org.typelevel.log4cats.Logger
import tsec.jwt.*
import tsec.jws.mac.*
import tsec.mac.jca.*

import scala.concurrent.duration.*


trait JwtUtils [F[_]]{
  def generateJwt(entity:(String,Json)): F[String]
  def verifyAndParseJwt(jwt:String):F[JWTMac[HMACSHA256]]
}

object JwtUtils{
  def apply[F[_]:Sync](sha256Key:String)(using logger:Logger[F]) = new JwtUtils[F]:
    override def generateJwt(entity: (String,Json)): F[String] = for{
      key <- HMACSHA256.buildKey[F](sha256Key.getBytes)
      claims <- JWTClaims.withDuration(customFields = Seq(entity))
      jwtString <- JWTMac.buildToString[F,HMACSHA256](claims,key)
    } yield jwtString

    override def verifyAndParseJwt(jwt: String): F[JWTMac[HMACSHA256]] = for{
      key <- HMACSHA256.buildKey[F](sha256Key.getBytes)
      jwtMac <- JWTMac.verifyAndParse[F,HMACSHA256](jwt,key)
    } yield jwtMac
}
