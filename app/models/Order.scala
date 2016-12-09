package models

import play.api.libs.json.Json

case class Order(
                  id: Long,
                  externalId: String,
                  timeStamp: Long,
                  num: String,
                  author: User,
                  contragent: Contragent,
                  orderLines: List[OrderLine] )

case class OrderLine(
                      item: Item,
                      amount: Double,
                      price: Long,
                      sum: Long)

object Order {
  implicit val OrderLineFormat = Json.format[OrderLine]
  implicit val OrderFormatter = Json.format[Order]
}





