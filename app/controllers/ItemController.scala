package controllers

import javax.inject._

import models.Item
import play.api.libs.json._
import play.api.mvc._
import repos.MongoItemsRepo
import utils.Errors

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItemController @Inject()(val itemsRepo: MongoItemsRepo)
                              (implicit exec: ExecutionContext)
  extends Controller {


  def createItems = Action.async(parse.json) { request =>
    Json.fromJson[Seq[Item]](request.body) match {
      case JsSuccess(newItems, _) =>
        itemsRepo.create(newItems).map {
          result => Created(s"Created $result items")
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a items from the json provided. "
          + Errors.show(errors)))
    }
  }

  def readItems = Action.async {
    itemsRepo.readAll().map { items =>
      Ok(Json.toJson(items))
    }
  }
}