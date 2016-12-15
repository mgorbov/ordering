package service

import models.{Order, User}

trait OrderService {

  def createOrder(order: Order, user: User)

  def createOrders(orders: Seq[Order])

  def deleteOrder()
}
