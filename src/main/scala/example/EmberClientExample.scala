package example

import cats.effect._
import cats.effect.std.Console
import org.http4s.ember.client.EmberClientBuilder
import org.http4s._
import org.http4s.client.Client
import org.http4s.syntax.all._
import org.http4s.circe._
import io.circe._
import epollcat.EpollApp
import fs2.io.net.tls.S2nConfig
import fs2.io.net.tls.TLSContext

object EmberClientExample extends EpollApp {

  final case class Joke(joke: String)
  object Joke {
    implicit val jokeDecoder: Decoder[Joke] = Decoder.derived[Joke]
    implicit def jokeEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Joke] =
      jsonOf
  }

  def run(args: List[String]): IO[ExitCode] = customTLS
    .flatMap(createClient)
    .use{ client =>
      client.expect[Joke](Request(Method.GET, uri"https://icanhazdadjoke.com/"))
        .flatTap(j => Console[IO].println(j.joke))
    }.as(ExitCode.Success)

    def createClient(tlsContext: TLSContext[IO]): Resource[IO, Client[IO]] = {
      EmberClientBuilder
        .default[IO]
        .withTLSContext(tlsContext)
        .withHttp2
        .build
    }

    // TLS 1.3 is not supported without a different default
    def customTLS =
      S2nConfig.builder
        .withCipherPreferences("default_tls13")
        .build[IO]
        .map(TLSContext.Builder.forAsync[IO].fromS2nConfig(_))

}