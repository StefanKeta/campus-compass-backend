package ro.campuscompass.regional.httpserver

import cats.*
import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import ro.campuscompass.common.http.ServerConfig
import ro.campuscompass.common.logging.*
import ro.campuscompass.regional.algebra.authorization.AuthorizationAlgebra
import ro.campuscompass.regional.algebra.university.UniversityAlgebra

class RegionalServer[F[_]: Async](
  serverConfig: ServerConfig,
  regionalServerRoutes: RegionalServerRoutes[F]
) extends Logging {
  def startServer: Resource[F, Server] = {
    val address = for {
      host <- Host.fromString(serverConfig.host).toRight(
        IllegalArgumentException(s"${serverConfig.host} does not represent a valid host")
      )
      port <-
        Port.fromInt(serverConfig.port).toRight(IllegalArgumentException(s"${serverConfig.port} does not represent a valid port"))
    } yield (host, port)

    for {
      address <- Resource.pure(address).rethrow
      (host, port) = address
      server <- EmberServerBuilder
        .default[F]
        .withHost(host)
        .withPort(port)
        .withHttpApp(regionalServerRoutes.http4sRoutes.orNotFound)
        .build
      _ <- logger[Resource[F, *]].info(s"Regional server started at: $host:$port")
    } yield server
  }
}

object RegionalServer {
  def start[F[_]: Async](serverConfig: ServerConfig)(
    authAlgebra: AuthorizationAlgebra[F],
    universityAlgebra: UniversityAlgebra[F],
    regionalApiKey: String,
  ): Resource[F, Server] =
    new RegionalServer(
      serverConfig,
      RegionalServerRoutes[F](authAlgebra, universityAlgebra, regionalApiKey)
    ).startServer
}
