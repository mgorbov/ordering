package repos

import javax.inject.Singleton

import com.google.inject.Inject
import models.Category
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MongoCategoriesRepo @Inject()(val reactiveMongoApi: ReactiveMongoApi)
                                   (implicit ec: ExecutionContext)
  extends MongoRepo[Category] {

  override protected val collectionFuture: Future[JSONCollection] =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("categories"))
}

