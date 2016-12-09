package models

import play.api.libs.json.Json

case class Item(
                 id: Long,
                 externalId: String,
                 categoryId: Long,
                 code: String,
                 article: String,
                 name: String,
                 price: Long,
                 stock: Double)

object Item {
  implicit val formatter = Json.format[Item]
}

