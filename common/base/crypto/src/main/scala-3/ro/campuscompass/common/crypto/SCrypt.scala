package ro.campuscompass.common.crypto

import cats.effect.Sync
import cats.implicits.*
import tsec.passwordhashers.*
import tsec.passwordhashers.jca.*

object SCrypt {
  def check[F[_]: Sync](password: String, hash: String): F[Boolean] =
    jca.SCrypt.checkpwBool[F](password, PasswordHash[jca.SCrypt](hash))

  def hash[F[_]: Sync](password: String): F[String] =
    jca.SCrypt.hashpw[F](password).map(identity)
}
