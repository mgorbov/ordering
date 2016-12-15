package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import models.{Category, Role}
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, Controller}
import repos.MongoCategoriesRepo
import utils.Errors
import utils.auth.{DefaultEnv, WithRole}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryController @Inject()(
                                    val categoriesRepo: MongoCategoriesRepo,
                                    silhouette: Silhouette[DefaultEnv]
                                  )(implicit ec: ExecutionContext)
  extends Controller {

  import silhouette.SecuredAction

  def createCategories() = SecuredAction(WithRole(Role.admin)).async(parse.json) {
    request => Json.fromJson[Seq[Category]](request.body) match {
      case JsSuccess(newCategories, _) =>
        categoriesRepo.create(newCategories).map { _ => Created("Ok") }
      case JsError(errors) =>
        Logger.error("Could not build a category from the json provided. "
          + Errors.show(errors))
        Future.successful(BadRequest("Could not build a city from the json provided. " +
          Errors.show(errors)))
    }
  }

  def readCategories = SecuredAction(WithRole(Role.user)).async {
    categoriesRepo.readAll().map {
      categories => Ok(Json.toJson(categories))
    }
  }

  def deleteCategories() = SecuredAction(WithRole(Role.admin)).async {
    categoriesRepo.delete().map {
      _ => Ok("Deleted")
    }
  }
}
