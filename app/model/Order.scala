package model

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
  implicit val formatter = Json.format[Order]
}

object OrderLine {
  implicit val formatter = Json.format[OrderLine]
}




