package example

import cats.syntax.all._
import cats.effect._
import cats.effect.std.Console
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s._
import org.http4s.client.Client
import org.http4s.syntax.all._
import org.http4s.circe._
import org.http4s.dsl.request._
import io.circe._
import epollcat.EpollApp
import fs2.io.net.tls.S2nConfig
import fs2.io.net.tls.TLSContext

object EmberExample extends EpollApp {

  final case class Joke(joke: String)
  object Joke {
    implicit val jokeDecoder: Decoder[Joke] = Decoder.derived[Joke]
    implicit def jokeEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Joke] =
      jsonOf
  }

  def run(args: List[String]): IO[ExitCode] = customTLS
    .flatMap(createClient)
    .use{ client =>
      createServer(client).useForever
    }.as(ExitCode.Success)


  def createServer(client: Client[IO]): Resource[IO, Unit] =
    EmberServerBuilder.default[IO]
      .withHttp2
      .withHttpApp(app(client).orNotFound)
      .build
      .void

  def app(client: Client[IO]) = HttpRoutes.of[IO]{
    case GET -> Root =>
      Response[IO](Status.Ok).withEntity("Hey There!").pure[IO]
    case GET -> Root / "hello" / person =>
      Response[IO](Status.Ok).withEntity(s"Hello, $person").pure[IO]
    case GET -> Root / "joke" => getJoke(client).map(Response(Status.Ok).withEntity(_))
  }

  def getJoke(client: Client[IO]): IO[String] =
    client.expect[Joke](Request(Method.GET, uri"https://icanhazdadjoke.com/"))
      .map(_.joke)

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

object EmberServerExample