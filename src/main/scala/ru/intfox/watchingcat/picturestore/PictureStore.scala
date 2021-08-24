package ru.intfox.watchingcat.picturestore

import cats.effect.IO
import fs2.Stream
import org.http4s.{EntityDecoder, Request, Uri}
import org.http4s.client.Client
import ru.intfox.watchingcat.Conf.CatPicturePath
import fs2.io.file.Files
import fs2.io.file.Path

import java.io.File

trait PictureStore[F[_]] {
  def uploadFromUrl(url: String, fileName: String): F[Unit]

  def getListPictures(): F[List[String]]
}

object PictureStore {
  def apply(catPicturePath: CatPicturePath, client: Client[IO]): PictureStore[IO] = new PictureStore[IO] {
    override def uploadFromUrl(url: String, fileName: String): IO[Unit] =  for {
      _ <- client.expectOr(url)(_ => IO(new UpstreamError))(EntityDecoder.binFile(new File(s"${catPicturePath.path.toString}/$fileName")))
    } yield ()

//    private def getCat(url: String): IO[Stream[IO, Byte]] =
//      for {
//        uri <- IO.fromEither(Uri.fromString(url))
////        req = client.stream(Request(uri = uri))
//        req <- client.expectOr[Unit](uri)
//      } yield res
//
//    private def saveCat(cat: Stream[IO, Byte], fileName: String): IO[Unit] =
//      Files[IO].writeAll(Path(s"${catPicturePath.path.toString}/$fileName"))(cat).compile.drain
//
    override def getListPictures(): IO[List[String]] = IO(
      catPicturePath.path.toFile.listFiles().map(_.getName).toList
    )
  }

  class UpstreamError extends Throwable("Upload error")
}


