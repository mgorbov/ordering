package dao

import model.Category
import reactivemongo.api.commands.MultiBulkWriteResult

import scala.concurrent.Future

trait CategoryDAO {
  def createCategories(newCategories: Seq[Category]): Future[MultiBulkWriteResult]

  def readItems()
}
