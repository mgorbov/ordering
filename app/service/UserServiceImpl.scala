package service

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import play.api.libs.json.Json
import repos.MongoUserRepo

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl @Inject() (userRepo: MongoUserRepo)
                                (implicit ex: ExecutionContext)
  extends UserService {
  /**
    * Saves a user.
    *
    * @param user The user to save.
    * @return The saved user.
    */
  override def save(user: User): Future[User] =
    userRepo.create(user).map( _ => user) //TODO STUB, probably we should generate "userID"


  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    userRepo.readOne(Json.obj("loginInfo" -> loginInfo))
}
