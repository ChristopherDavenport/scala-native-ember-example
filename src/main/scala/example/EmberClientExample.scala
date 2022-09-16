package example

import cats.effect._
import cats.effect.std.Console
import org.http4s.ember.client.EmberClientBuilder
import org.http4s._
import org.http4s.syntax.all._
import org.http4s.circe._
import io.circe._
import epollcat.EpollApp

object EmberClientExample extends EpollApp {

  final case class Joke(joke: String)
  object Joke {
    implicit val jokeDecoder: Decoder[Joke] = Decoder.derived[Joke]
    implicit def jokeEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Joke] =
      jsonOf
  }

  def run(args: List[String]): IO[ExitCode] = EmberClientBuilder
    .default[IO]
    .withHttp2
    .build
    .use{ client =>
      client.expect[Joke](Request(Method.GET, uri"https://icanhazdadjoke.com/"))
        .flatTap(j => Console[IO].println(j.joke))
    }.as(ExitCode.Success)

}