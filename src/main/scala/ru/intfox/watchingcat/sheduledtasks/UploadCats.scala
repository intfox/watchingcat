package ru.intfox.watchingcat.sheduledtasks

import cats.effect.IO
import cats.effect.implicits.concurrentParTraverseOps
import cats.implicits._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import ru.intfox.watchingcat.Conf.UploadCatsConf
import ru.intfox.watchingcat.catsstore.CatsStore
import ru.intfox.watchingcat.picturestore.PictureStore
import ru.intfox.watchingcat.thecatapi.TheCatApi

import scala.concurrent.duration.FiniteDuration

class UploadCats(theCatApi: TheCatApi[IO], pictureStore: PictureStore[IO], catsStore: CatsStore[IO], conf: UploadCatsConf) extends Task {
  private val logger = Slf4jLogger.getLogger[IO]

  override def run(): IO[Unit] = for {
    numberCats <- catsStore.getNumberNotViewedCat()
    _ <- if (numberCats < conf.localStoreMinSize) for {
      _ <- logger.info("Load cats")
      cats <-
        if(conf.localStoreMinSize > 100) (1 to conf.localStoreMinSize / 100).map(_ => theCatApi.search(100)).toList.sequence.map(_.flatten)
        else theCatApi.search(conf.localStoreMinSize)
      _ <- (cats.map {
        cat =>
          val fileName = cat.url.split("/").last
          (for {
            _ <- pictureStore.uploadFromUrl(cat.url, fileName)
            _ <- catsStore.insertCat(cat.id, cat.url, fileName)
          } yield ()).handleErrorWith(e => IO.raiseError(new Throwable(s"error load cat $cat: ${e.getMessage}")))
      }).parTraverseN(10)(_.handleErrorWith(e => logger.error(e.getMessage)))
    } yield () else IO.unit
  } yield ()

  override def sleepTime(): FiniteDuration = conf.regularity
}
