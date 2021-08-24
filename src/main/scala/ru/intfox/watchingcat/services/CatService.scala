package ru.intfox.watchingcat.services

import cats.effect.IO
import org.http4s.{Response, Uri}
import ru.intfox.watchingcat.catsstore.CatsStore
import org.http4s.implicits._
import org.http4s.dsl.io._
import org.http4s.headers.Location

trait CatService[F[_]] {
  def getCat(): F[Response[F]]
}

object CatService {
  def apply(catsStore: CatsStore[IO]): CatService[IO] = new CatService[IO] {
    override def getCat(): IO[Response[IO]] = for {
      cat <- catsStore.getNotViewedCat()
      uri = Uri.unsafeFromString(s"/cat/${cat.filename}")
      response <- Found(Location(uri))
    } yield response
  }
}
