//> using dep org.http4s::http4s-ember-client::0.23.25
//> using dep org.http4s::http4s-ember-server::0.23.25
//> using dep org.http4s::http4s-dsl::0.23.25
//> using dep com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-core::2.27.5
//> using dep com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-macros::2.27.5
//> using dep ch.qos.logback:logback-classic:1.4.14
//> using dep io.indigoengine::tyrian::0.10.0

//> using option -Werror -Wunused:all

package no.hnikt

import cats.effect.IOApp
import cats.effect.IO
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.*
import org.http4s.server.middleware.Logger
import scala.concurrent.duration.*

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    (for
      client <- EmberClientBuilder
        .default[IO]
        .build // Om vi trenger en http klient

      httpApp = {
        routes.index
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
