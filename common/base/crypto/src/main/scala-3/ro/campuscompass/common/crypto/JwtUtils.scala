package ro.campuscompass.common.crypto

import cats.effect.Sync
import cats.syntax.all.*
import io.circe.Json
import tsec.jws.mac.*
import tsec.jwt.*
import tsec.mac.jca.*
import io.circe.generic.auto.*
import io.circe.syntax.*

import scala.concurrent.duration.*

object JwtUtils {
  
  def generateJwt[F[_]: Sync](jwtConfig: JwtConfig)(values: (String, String)*): F[JWT] = for {
    key       <- HMACSHA256.buildKey[F](jwtConfig.sha256Key.getBytes)
    claims    <- JWTClaims.withDuration(customFields = Seq(("keys", values.toMap.asJson)))
    jwtString <- JWTMac.buildToString[F, HMACSHA256](claims, key)
  } yield JWT(jwtString)

  def verifyAndParseJwt[F[_]: Sync](jwt: JWT, jwtConfig: JwtConfig): F[JWTMac[HMACSHA256]] = for {
    key    <- HMACSHA256.buildKey[F](jwtConfig.sha256Key.getBytes)
    jwtMac <- JWTMac.verifyAndParse[F, HMACSHA256](jwt.value, key)
  } yield jwtMac

  def extractData[F[_]: Sync](jwtMac: JWTMac[HMACSHA256]): F[Map[String, String]] =
    jwtMac.body.getCustomF[F, Map[String, String]]("keys")
}
