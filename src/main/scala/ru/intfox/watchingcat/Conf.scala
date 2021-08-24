package ru.intfox.watchingcat

import cats.effect.IO
import pureconfig.generic.ProductHint
import ru.intfox.watchingcat.Conf.{Http, MongoConf, UploadCatsConf}

import scala.concurrent.duration.FiniteDuration
//import com.typesafe.config.ConfigOrigin
import pureconfig._
import pureconfig.error.{CannotConvert, ConfigReaderFailure, ConfigReaderFailures}
import pureconfig.generic.auto._
import ru.intfox.watchingcat.Conf.{CatPicturePath, TheCatApiConf}

import java.nio.file.Path

case class Conf(theCatApi: TheCatApiConf, catPicturePath: CatPicturePath, http: Http, mongo: MongoConf, uploadCats: UploadCatsConf)

object Conf {
  implicit def hint[A] = ProductHint[A](ConfigFieldMapping(CamelCase, SnakeCase))

  implicit val catPicturePathReader = ConfigReader[Path].emap {
    case path if path.toFile.isDirectory => Right(CatPicturePath(path))
    case path => Left(CannotConvert(path.toFile.getAbsolutePath, "CatPicturePath", "is not directory"))
  }

  def apply(): IO[Conf] = IO.fromEither(ConfigSource.file("watchingcat.conf").withFallback(ConfigSource.default).load[Conf].left.map(new ConfError(_)))

  case class TheCatApiConf(apiKey: String)

  case class CatPicturePath(path: Path)

  case class Http(port: Int, host: String)

  case class MongoConf(servers: List[Server], creds: Option[MongoCreds], db: Option[String])

  case class Server(host: String, port: Int)

  case class MongoCreds(username: String, password: String)

  case class UploadCatsConf(regularity: FiniteDuration, localStoreMinSize: Int)

  class ConfError(configReaderFailures: ConfigReaderFailures) extends Throwable(configReaderFailures.toString())
}
