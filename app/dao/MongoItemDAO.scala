package dao
import com.google.inject.Inject
import model.Item
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.commands.MultiBulkWriteResult

import scala.concurrent.Future

class MongoItemDAO @Inject()(
                             val reactiveMongoApi: ReactiveMongoApi)
  extends ItemDAO with ReactiveMongoComponents{
  override def createItems(newCategories: Seq[Item]): Future[MultiBulkWriteResult] = ???

  override def readItems(): Future[List[Item]] = ???
}
