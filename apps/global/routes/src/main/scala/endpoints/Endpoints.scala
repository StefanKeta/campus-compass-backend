package endpoints
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import io.circe.generic.auto._
import java.util.UUID

object Endpoints {
  val helloEndpoint = endpoint.in("api" / "v1").out(jsonBody[String])
}
