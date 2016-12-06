package dao

import javax.inject.Singleton

import com.google.inject.Inject
import model.Category
import play.api.libs.json.Json
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.ReadPreference
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

//implicit for converting data. Idea mistakenly mark import as unused
import reactivemongo.play.json._

@Singleton
class MongoCategoryDAO @Inject()(
                                  val reactiveMongoApi: ReactiveMongoApi
                                )
                                (implicit ec: ExecutionContext)
                                  extends CategoryDAO
                                    with ReactiveMongoComponents {
                                    //TODO try to remove ReactiveMongoComponents

  private[this] def categoryFuture: Future[JSONCollection] =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("category"))

  override def createCategories(newCategories: Seq[Category]): Future[_] =
    categoryFuture.flatMap { categories =>
      val documents = newCategories.map(implicitly[categories.ImplicitlyDocumentProducer](_))
      categories.bulkInsert(ordered = true)(documents: _*)
    }

  override def readCategories(): Future[List[Category]] =
    categoryFuture.flatMap { category =>
      category.find(Json.obj()).
        // perform the query and get a cursor of JsObject
        cursor[Category](ReadPreference.primary).
        // Coollect the results as a list
        collect[List]() //TODO update method according new features
        // collect[List](-1, Cursor.FailOnError())
    }
}