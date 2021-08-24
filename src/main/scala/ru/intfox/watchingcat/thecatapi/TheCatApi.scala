package ru.intfox.watchingcat.thecatapi

import ru.intfox.watchingcat.Conf._
import cats.effect.IO
import org.http4s.client.Client
import io.circe.generic.auto._
import org.http4s.{EntityDecoder, Header, Headers, Request, Uri, headers}
import org.http4s.circe._
import org.http4s.client.dsl.io
import org.typelevel.ci.CIStringSyntax
import org.http4s.circe.CirceEntityDecoder._

trait TheCatApi[F[_]] {
  def search(limit: Int): F[List[Cat]]
}

object TheCatApi {
  def apply(conf: TheCatApiConf, client: Client[IO]): TheCatApi[IO] = new TheCatApi[IO] {
    override def search(limit: Int): IO[List[Cat]] = client.expect[List[Cat]](Request[IO](
      uri = Uri.unsafeFromString(s"https://api.thecatapi.com/v1/images/search?limit=$limit"),
      headers = Headers(Header.Raw(ci"x-api-key", conf.apiKey))
    ))
  }
}

