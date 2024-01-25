package no.hnikt
package routes

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO
import tyrian.*
import tyrian.Html.*
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType
import org.http4s.Response

private[routes] def htmlResponse(html: String): IO[Response[IO]] =
  Ok(html).map(_.withContentType(`Content-Type`(MediaType.text.html)))

def index: HttpRoutes[IO] =
  val indexPage =
    "<!DOCTYPE html>" + html(
      head(
        meta(charset := "utf-8"),
        script(src := "https://unpkg.com/htmx.org@1.9.10")(),
        script(src := "https://unpkg.com/htmx.org/dist/ext/ws.js")()
      ),
      div(
        h1("Hello World!")
      )
    )

  HttpRoutes.of[IO]:
    case GET -> Root =>
      htmlResponse(indexPage)
