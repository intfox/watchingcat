package ru.intfox.watchingcat.catsstore

import cats.effect.IO
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.model.UpdateOptions
import org.mongodb.scala.{Document, MongoClient}
import ru.intfox.watchingcat.Error.NotFound
import ru.intfox.watchingcat.catsstore.CatsStore.Cat

trait CatsStore[F[_]] {
  def insertCat(id: String, url: String, filename: String): F[Unit]

  def getNotViewedCat(): F[Cat]

  def getNumberNotViewedCat(): F[Int]
}

object CatsStore {
  def apply(mongoClient: MongoClient): CatsStore[IO] = new CatsStore[IO] {
    private val db = mongoClient.getDatabase("watchingcat")
    private val catsCollection = db.getCollection[Document]("cats")

    val catCodecProvider = Macros.createCodecProvider[Cat]()

    override def insertCat(id: String, url: String, filename: String): IO[Unit] = IO.fromFuture(IO(catsCollection.updateOne(
      Document("_id" -> id),
      Document(
        "$setOnInsert" ->
          Document(
            "url" -> url,
            "filename" -> filename,
          ),
        "$inc" -> Document("views" -> 1)
      ),
      UpdateOptions().upsert(true)).head())).map(_ => ())

    override def getNotViewedCat(): IO[Cat] = for {
      docOpt <- IO.fromFuture(IO(catsCollection.findOneAndUpdate(Document("views" -> Document("$gt" -> 0)), Document("$inc" -> Document("views" -> -1))).headOption()))
      doc <- IO.fromOption(docOpt)(new CatNotFound())
      cat = Cat(
        _id = doc("_id").asString().getValue,
        url = doc("url").asString().getValue,
        filename = doc("filename").asString().getValue,
        views = doc("views").asInt32().getValue
      )
    } yield cat

    override def getNumberNotViewedCat(): IO[Int] = for {
      numb <- IO.fromFuture(IO(catsCollection.countDocuments(Document("views" -> Document("$gt" -> 0))).head()))
    } yield numb.toInt
  }

  case class Cat(_id: String, url: String, filename: String, views: Int)

  class CatNotFound() extends NotFound
}


