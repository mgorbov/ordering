package models

import play.api.libs.json.Json

case class Category(
                     id: Long,
                     externalId: String,
                     name: String,
                     $$treeLevel: Int
                   )

object Category {
  implicit val formatter = Json.format[Category]
}
