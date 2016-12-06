package dao

import model.Item
import reactivemongo.api.commands.MultiBulkWriteResult

import scala.concurrent.Future

trait ItemDAO {
  def createItems(newCategories: Seq[Item]): Future[MultiBulkWriteResult]

  def readItems(): Future[List[Item]]
}
