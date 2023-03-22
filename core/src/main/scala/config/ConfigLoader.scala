package config
import cats.effect.IO
import cats.effect.kernel.Sync
import cats.implicits.*
import config.domain.*
import org.typelevel.log4cats.Logger
import pureconfig.*
import pureconfig.generic.derivation.default.*
import pureconfig.module.catseffect.syntax.*

trait ConfigLoader[F[_]] {
  def load(): F[AppConfiguration]
}

object ConfigLoader {
  def apply[F[_]](using S:Sync[F],logger: Logger[F]): ConfigLoader[F] = new ConfigLoader[F]:
    override def load(): F[AppConfiguration] = for{
      _ <- logger.info(s"${getClass}:Configuration loaded")
      configs <- ConfigSource.default.loadF[F,AppConfiguration]()
    } yield configs
}
