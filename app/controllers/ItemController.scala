package controllers

import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import models.{Item, Role}
import play.api.libs.json._
import play.api.mvc._
import repos.MongoItemsRepo
import utils.Errors
import utils.auth.{DefaultEnv, WithRole}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItemController @Inject()(
                                val itemsRepo: MongoItemsRepo,
                                silhouette: Silhouette[DefaultEnv]
                              )
                              (implicit exec: ExecutionContext)
  extends Controller {

  import silhouette.SecuredAction

  def createItems = SecuredAction(WithRole(Role.admin)).async(parse.json)  { request =>
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

  def readItems = SecuredAction(WithRole(Role.user)).async {
    itemsRepo.readAll().map { items =>
      Ok(Json.toJson(items))
    }
  }
}