package ro.campuscompass.common.crypto

import cats.effect.Sync
import cats.syntax.all.*
import io.circe.Json
import tsec.jws.mac.*
import tsec.jwt.*
import tsec.mac.jca.*

import scala.concurrent.duration.*

object JwtUtils {

  def generateJwt[F[_]: Sync](entity: (String, Json), jwtConfig: JwtConfig): F[JWT] = for {
    key       <- HMACSHA256.buildKey[F](jwtConfig.sha256Key.getBytes)
    claims    <- JWTClaims.withDuration(customFields = Seq(entity))
    jwtString <- JWTMac.buildToString[F, HMACSHA256](claims, key)
  } yield JWT(jwtString)

  def verifyAndParseJwt[F[_]: Sync](jwt: String, jwtConfig: JwtConfig): F[JWTMac[HMACSHA256]] = for {
    key    <- HMACSHA256.buildKey[F](jwtConfig.sha256Key.getBytes)
    jwtMac <- JWTMac.verifyAndParse[F, HMACSHA256](jwt, key)
  } yield jwtMac
}
