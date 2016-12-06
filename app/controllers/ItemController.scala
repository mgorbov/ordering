package controllers

import javax.inject._

import model.Item
import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.ReadPreference
import utils.Errors
import reactivemongo.play.json.collection._
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItemController @Inject()(val reactiveMongoApi: ReactiveMongoApi)
                              (implicit exec: ExecutionContext)
    extends Controller
      with MongoController
      with ReactiveMongoComponents {

  private[this] def itemsFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("item"))

  def createItems = Action.async(parse.json) { request =>
    Json.fromJson[Seq[Item]](request.body) match {
      case JsSuccess(newCities, _) =>
        itemsFuture.flatMap { items =>
          val documents = newCities.map(implicitly[items.ImplicitlyDocumentProducer](_))
          items.bulkInsert(ordered = true)(documents: _*).map { multiResult =>
            Logger.debug(s"Successfully inserted with multiResult: $multiResult")
            Created(s"Created ${multiResult.n} cities")
          }
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a city from the json provided. " + Errors.show(errors)))
    }
  }

  def readItems = Action.async {
    // let's do our query
    val futureCitiesList: Future[List[Item]] = itemsFuture.flatMap {
      _.find(Json.obj()).
        // perform the query and get a cursor of JsObject
        cursor[Item](ReadPreference.primary).
        // Coollect the results as a list
        collect[List]() //TODO update method according new features
    }
    // everything's ok! Let's reply with a JsValue
    futureCitiesList.map { cities =>
      Ok(Json.toJson(cities))
    }
  }
}