package ru.intfox.watchingcat

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.effect.implicits._
import org.http4s.{HttpRoutes, Response, ResponseCookie}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.server.staticcontent.{FileService, fileService}
import org.http4s.implicits._
import org.mongodb.scala.MongoClient
import ru.intfox.watchingcat.catsstore.CatsStore
import ru.intfox.watchingcat.picturestore.PictureStore
import ru.intfox.watchingcat.services.CatService
import ru.intfox.watchingcat.sheduledtasks.{Scheduler, Task, UploadCats}
import ru.intfox.watchingcat.thecatapi.TheCatApi

import scala.concurrent.ExecutionContext.global

object Server extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = (for {
    conf <- Resource.eval(Conf())
    httpClient <- BlazeClientBuilder[IO](global).resource
    theCatApi = TheCatApi(conf.theCatApi, httpClient)
    pictureStore = PictureStore(conf.catPicturePath, httpClient)
    mongoClient =
      MongoClient(s"mongodb://${conf.mongo.creds.map(c => s"${c.username}:${c.password}@").getOrElse("")}${conf.mongo.servers.map(s => s"${s.host}:${s.port}").mkString(",")}/${conf.mongo.db.getOrElse("")}")
    catsStore = CatsStore(mongoClient)
    catService = CatService(catsStore)
    catApp = HttpRoutes.of[IO] {
      case GET -> Root => catService.getCat().handleErrorWith {
        case e: Error.HttpError => IO(Response[IO](e.status))
        case e => IO.raiseError(e)
      }
    }
    httpApp = Router(
      "cat" -> catApp,
      "cat" -> fileService[IO](FileService.Config(conf.catPicturePath.path.toAbsolutePath.toString))
    ).orNotFound
    _ <- Resource.eval(Scheduler(List(new UploadCats(theCatApi, pictureStore, catsStore, conf.uploadCats)))).start
    _ <- BlazeServerBuilder[IO](global).bindHttp(port = conf.http.port, host = conf.http.host).withHttpApp(httpApp).resource
  } yield ()).use(_ => IO.never).as(ExitCode.Success)
}
