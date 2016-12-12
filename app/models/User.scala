package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import models.Role.Role
import play.api.libs.json.Json

object Role extends Enumeration {
  type Role = Value
  val unauthorized, user, admin = Value
}

case class User(
                 userID: UUID,
                 loginInfo: LoginInfo,
                 firstName: Option[String],
                 lastName: Option[String],
                 email: Option[String],
                 role: Option[Role]
               ) extends Identity

object User {
  implicit val formatterUser = Json.format[User]
  implicit val formatterRole = Json.format[Role]
}

