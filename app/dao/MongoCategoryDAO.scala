package dao

import com.google.inject.Inject
import model.Category
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoComponents
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import play.modules.reactivemongo._
import reactivemongo.api.commands.MultiBulkWriteResult

class MongoCategoryDAO @Inject()(
                                  val reactiveMongoApi: ReactiveMongoApi)
  extends CategoryDAO
    with ReactiveMongoComponents {

  private[this] def categoryFuture: Future[JSONCollection] =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("category"))

  override def createCategories(newCategories: Seq[Category]): Future[MultiBulkWriteResult] =
    categoryFuture.flatMap { categories =>
      val documents = newCategories.map(implicitly[categories.ImplicitlyDocumentProducer](_))
      categories.bulkInsert(ordered = true)(documents: _*).map { multiResult =>
        Logger.debug(s"Successfully inserted with multiResult: $multiResult")
        multiResult
      }
    }

  override def readItems(): Unit = ???
}
