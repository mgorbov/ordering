package utils.auth

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.Role.Role
import models.{Role, User}
import play.api.mvc.Request

import scala.concurrent.Future

case class WithRole(role: Role) extends Authorization[User, JWTAuthenticator] {
  override def isAuthorized[B](user: User, authenticator: JWTAuthenticator)
                              (implicit request: Request[B]): Future[Boolean] = {

    Future.successful(user.role.getOrElse(Role.unauthorized).id >= role.id)
  }
}
