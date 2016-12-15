package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import models.{Order, Role}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.Controller
import repos.MongoOrdersRepo
import utils.Errors
import utils.auth.{DefaultEnv, WithRole}

import scala.concurrent.{ExecutionContext, Future}

//TODO separate into controller and service
@Singleton
class OrderController @Inject()(
                                 val ordersRepo: MongoOrdersRepo,
                                 silhouette: Silhouette[DefaultEnv]
                               )
                               (implicit exec: ExecutionContext)
  extends Controller {

  import silhouette.SecuredAction


  def createOrder = SecuredAction(WithRole(Role.user)).async(parse.json) { request =>
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

  def createOrders = SecuredAction(WithRole(Role.user)).async(parse.json) { request =>
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

  def readOrders = SecuredAction(WithRole(Role.user)).async {
    ordersRepo.readAll().map {
      orders => Ok(Json.toJson(orders))
    }
  }
}
