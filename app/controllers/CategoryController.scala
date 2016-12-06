package controllers

import javax.inject.{Inject, Singleton}

import dao.CategoryDAO
import model.Category
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, Controller}
import utils.{Errors, JsonUtils}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryController @Inject()(
                                    val categoryDAO: CategoryDAO
                                  )
                                  (implicit ec: ExecutionContext)
                                  extends Controller {

  def createCategories = Action.async(parse.json) { request =>
//    val newCategories = JsonUtils.jsonToModel[Seq[Category]](request.body)
//    categoryDAO.createCategories(newCategories).map { _ => Created("Ok") }
    Json.fromJson[Seq[Category]](request.body) match {
      case JsSuccess(newCategories, _) =>
        categoryDAO.createCategories(newCategories).map { _ => Created("Ok") }
      case JsError(errors) =>
        Logger.error("Could not build a category from the json provided. " + Errors.show(errors))
        Future.successful(BadRequest("Could not build a city from the json provided. " +
          Errors.show(errors)))
    }

  }

  def readCategories = Action.async {
    categoryDAO.readCategories().map { cities =>
      Ok(Json.toJson(cities))
    }
  }
}
