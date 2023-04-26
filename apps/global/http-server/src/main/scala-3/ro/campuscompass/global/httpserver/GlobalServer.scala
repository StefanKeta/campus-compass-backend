package ro.campuscompass.global.httpserver

import cats.*
import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import ro.campuscompass.common.http.ServerConfig
import ro.campuscompass.common.logging.*
import ro.campuscompass.global.algebra.admin.{ AdminAlgebra, AdminConfig }
import ro.campuscompass.global.algebra.auth.AuthAlgebra
import ro.campuscompass.global.algebra.university.UniversityAlgebra

class GlobalServer[F[_]: Async](
  serverConfig: ServerConfig,
  globalServerRoutes: GlobalServerRoutes[F]
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
        .withHttpApp(globalServerRoutes.http4sRoutes.orNotFound)
        .build
      _ <- logger[Resource[F, *]].info(s"Global server started at: $host:$port")
    } yield server
  }
}

object GlobalServer {
  def start[F[_]: Async](
    serverConfig: ServerConfig,
    adminConfig: AdminConfig
  )(
    adminAlgebra: AdminAlgebra[F],
    authAlgebra: AuthAlgebra[F],
    universityAlgebra: UniversityAlgebra[F]
  ): Resource[F, Server] =
    new GlobalServer(
      serverConfig,
      GlobalServerRoutes[F](adminConfig)(
        adminAlgebra,
        authAlgebra,
        universityAlgebra
      )
    ).startServer
}
