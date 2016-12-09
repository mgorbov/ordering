package models

import play.api.libs.json.Json

case class Contragent(id: Long, externalId: String, name: String)

object Contragent {
  implicit val formatter = Json.format[Contragent]
}
