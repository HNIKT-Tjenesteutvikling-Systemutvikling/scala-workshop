//> using dep org.http4s::http4s-ember-client::0.23.25
//> using dep org.http4s::http4s-ember-server::0.23.25
//> using dep org.http4s::http4s-dsl::0.23.25
//> using dep com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-core::2.27.6
//> using dep com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-macros::2.27.6
//> using dep ch.qos.logback:logback-classic:1.4.14
//> using dep io.indigoengine::tyrian::0.10.0
//> using dep io.indigoengine::tyrian-htmx::0.10.0
//> using dep org.typelevel::log4cats-slf4j::2.6.0

//> using option -Werror -Wunused:all

package no.hnikt

import cats.effect.IOApp
import cats.effect.IO
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.staticcontent.*
import com.comcast.ip4s.*
import org.http4s.server.middleware.Logger
import scala.concurrent.duration.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import cats.effect.kernel.Ref
import no.hnikt.templates.Item
import cats.effect.kernel.Resource
import org.http4s.server.Router

object Main extends IOApp.Simple:
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

  override def run: IO[Unit] =
    (for
      client <- EmberClientBuilder
        .default[IO]
        .build // Om vi trenger en http klient

      items <- Resource.eval(Ref[IO].of(List.empty[Item]))
      active <- Resource.eval(Ref[IO].of(templates.ActiveFilter.All))

      httpApp = {
        Router(
          "/" -> routes.index(items, active),
          "assets" -> fileService[IO](FileService.Config("./static"))
        )
      }.orNotFound

      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalHttpApp)
        .withShutdownTimeout(1.second)
        .build
    yield ()).useForever
