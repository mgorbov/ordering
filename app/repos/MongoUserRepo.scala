package repos

import com.google.inject.Inject
import models.User
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

class MongoUserRepo @Inject()(val reactiveMongoApi: ReactiveMongoApi)
                             (implicit ec: ExecutionContext)
  extends MongoRepo[User] {

  override protected val collectionFuture: Future[JSONCollection] =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("orders"))
}
