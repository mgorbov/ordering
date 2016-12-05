package controllers

import javax.inject.{Inject, Singleton}

import model.Order
import model.OrderLine._
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.ReadPreference

import reactivemongo.play.json._ , collection._

import utils.Errors
import scala.concurrent.{ExecutionContext, Future}



@Singleton
class OrderController @Inject()(val reactiveMongoApi: ReactiveMongoApi)
                                  (implicit exec: ExecutionContext)
  extends Controller
    with MongoController
    with ReactiveMongoComponents {

  private[this] def orderFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("order"))

  def createOrder =  Action.async(parse.json) { request =>
    Json.fromJson[Order](request.body) match {
      case JsSuccess(order, _) =>
        for {
          orders <- orderFuture
          lastError <- orders.insert(order)
        } yield {
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created("Created 1 order")
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a order from the json provided. " + Errors.show(errors)))
    }
  }

  def createOrders = Action.async(parse.json) { request =>
    Json.fromJson[Seq[Order]](request.body) match {
      case JsSuccess(newCities, _) =>
        orderFuture.flatMap { orders =>
          val documents = newCities.map(implicitly[orders.ImplicitlyDocumentProducer](_))
          orders.bulkInsert(ordered = true)(documents: _*).map { multiResult =>
            Logger.debug(s"Successfully inserted with multiResult: $multiResult")
            Created(s"Created ${multiResult.n} orders")
          }
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a order from the json provided. " + Errors.show(errors)))
    }
  }

  def readOrders = Action.async {
    // let's do our query
    val futureOrderList: Future[List[Order]] = orderFuture.flatMap {
      _.find(Json.obj()).
        // perform the query and get a cursor of JsObject
        cursor[Order](ReadPreference.primary).
        // Collect the results as a list
        collect[List]() //TODO update method according new features
    }
    // everything's ok! Let's reply with a JsValue
    futureOrderList.map { orders => Ok(Json.toJson(orders)) }
  }
}
