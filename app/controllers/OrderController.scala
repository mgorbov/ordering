package controllers

import javax.inject.{Inject, Singleton}

import models.Order
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, Controller}
import repos.MongoOrdersRepo
import utils.Errors

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class OrderController @Inject()(val ordersRepo: MongoOrdersRepo)
                               (implicit exec: ExecutionContext)
  extends Controller {

  def createOrder = Action.async(parse.json) { request =>
    Json.fromJson[Order](request.body) match {
      case JsSuccess(newOrder, _) =>
        ordersRepo.create(newOrder) map {
          _ => Created("Created 1 order")
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a order from the json provided. "
          + Errors.show(errors)))
    }
  }

  def createOrders = Action.async(parse.json) { request =>
    Json.fromJson[Seq[Order]](request.body) match {
      case JsSuccess(newOrders, _) =>
        ordersRepo.create(newOrders).map {
          result => Created(s"Created $result orders")
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a order from the json provided. "
          + Errors.show(errors)))
    }
  }

  def readOrders = Action.async {
    ordersRepo.readAll().map {
      orders => Ok(Json.toJson(orders))
    }
  }
}
