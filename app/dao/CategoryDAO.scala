package dao

import com.google.inject.ImplementedBy
import model.Category

import scala.concurrent.Future

@ImplementedBy(classOf[MongoCategoryDAO])
trait CategoryDAO {
  def createCategories(newCategories: Seq[Category]): Future[_]

  def readCategories(): Future[List[Category]]
}
