package repos

import javax.inject.Singleton

import com.google.inject.Inject
import models.Order
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MongoOrdersRepo @Inject()(val reactiveMongoApi: ReactiveMongoApi)
                               (implicit ec: ExecutionContext)
  extends MongoRepo[Order] {

  override protected val collectionFuture: Future[JSONCollection] =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("orders"))
}



