package repos

import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.play.json.JSONSerializationPack.{Reader, Writer}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

//implicit conversion BSON <-> JSON
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

abstract class MongoRepo[T] {

  protected val collectionFuture: Future[JSONCollection]

  def create(newData: T)(implicit writer: Writer[T], ec: ExecutionContext): Future[_] =
    collectionFuture.map { collection =>
      collection.insert(newData).map { writeResult =>
        Logger.debug(s"Successfully inserted with writeResult: $writeResult")
        writeResult
      }
    }

  def create(newData: Seq[T])(implicit writer: Writer[T], ec: ExecutionContext): Future[Int] =
    collectionFuture.flatMap { collection =>
      val documents = newData.map( implicitly[collection.ImplicitlyDocumentProducer](_))
      collection.bulkInsert(ordered = true)(documents: _*) map { multiResult =>
        Logger.debug(s"Successfully inserted with multiResult: $multiResult")
        multiResult.n
      }
    }

  def readOne(query: JsObject = Json.obj())(implicit reader: Reader[T], ec: ExecutionContext) : Future[Option[T]] =
    collectionFuture.flatMap { collection =>
      collection.find(query).
        cursor[T]().
        collect[List](1, Cursor.FailOnError[List[T]]()).
        map { result =>
          Logger.debug(s"Successfully read: ${result.headOption}")
          result.headOption
        }
    }

  def readAll(query: JsObject = Json.obj())(implicit reader: Reader[T], ec: ExecutionContext) : Future[List[T]] =
    collectionFuture.flatMap { collection =>
      collection.find(query).
        cursor[T]().
        collect[List](1, Cursor.FailOnError[List[T]]()).
        andThen {
          case Success(result) => Logger.debug(s"Successfully read: $result")
        }
    }

  def delete(query: JsObject = Json.obj())(implicit writer: Writer[T], ec: ExecutionContext): Future[_] =
    collectionFuture.flatMap { collection =>
      collection.remove(query).map { writeResult =>
        Logger.debug(s"Successfully removed with writeResult: $writeResult")
      }
    }
}
