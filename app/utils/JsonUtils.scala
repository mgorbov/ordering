package utils

import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

object JsonUtils {
  def jsonToModel[T](jsValue: JsValue): T = {
    Json.fromJson[T](jsValue) match {
      case JsSuccess(newData, _) =>
        newData
      case JsError(errors) =>
        Logger.error("Could not build a category from the json provided. " + Errors.show(errors))
        throw new RuntimeException("Could not build a category from the json provided. "
          + Errors.show(errors))
    }
  }
}
