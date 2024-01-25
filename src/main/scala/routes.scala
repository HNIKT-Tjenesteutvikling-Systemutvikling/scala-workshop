package no.hnikt
package routes

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO
import no.hnikt.extensions.JsonCodec.given
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec

case class HelloWorldResponse(
    message: String
)

object HelloWorldResponse:
  given JsonValueCodec[HelloWorldResponse] = JsonCodecMaker.make

def helloWorldRoutes: HttpRoutes[IO] =
  HttpRoutes.of[IO]:
    case GET -> Root / "hello" =>
      Ok(HelloWorldResponse("Hello World!"))
