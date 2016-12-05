package controllers

import javax.inject.{Inject, Singleton}

import model.Category
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo._
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import utils.Errors

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryController @Inject()(val reactiveMongoApi: ReactiveMongoApi)
  (implicit exec: ExecutionContext)
    extends Controller
      with MongoController
      with ReactiveMongoComponents {

  private[this] def categoryFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("category"))

  def createCategories = Action.async(parse.json) { request =>
    Json.fromJson[Seq[Category]](request.body) match {
      case JsSuccess(newCategories, _) =>
        categoryFuture.flatMap { categories =>
          val documents = newCategories.map(implicitly[categories.ImplicitlyDocumentProducer](_))
          categories.bulkInsert(ordered = true)(documents: _*).map { multiResult =>
            Logger.debug(s"Successfully inserted with multiResult: $multiResult")
            Created(s"Created ${multiResult.n} categories")
          }
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a city from the json provided. " + Errors.show(errors)))
    }
  }

  def readCategories = Action.async {
    // let's do our query
    val futureCategoriesList: Future[List[Category]] = categoryFuture.flatMap {
      _.find(Json.obj()).
        // perform the query and get a cursor of JsObject
        cursor[Category](ReadPreference.primary).
        // Coollect the results as a list
        collect[List]() //TODO update method according new features
//      collect[List](-1, Cursor.FailOnError())
    }
    // everything's ok! Let's reply with a JsValue
    futureCategoriesList.map { cities =>
      Ok(Json.toJson(cities))
    }
  }
}
