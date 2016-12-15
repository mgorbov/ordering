package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json._

object Role extends Enumeration {
  type Role = Value

  val unauthorized = Value("unauthorized")
  val user = Value("user")
  val admin = Value("admin")

  implicit val formatterRole = {
    val writes = Writes[Role.Value](role => JsString(role.id.toString))
    val reads = new Reads[Role.Value] {
      def reads(json: JsValue):JsResult[Role.Value] = JsSuccess(Role(json.toString().toInt))
    }
    Format[Role.Value](reads, writes)
  }
}

case class User(
                 userID: UUID,
                 loginInfo: LoginInfo,
                 firstName: Option[String],
                 lastName: Option[String],
                 email: Option[String],
                 role: Option[Role.Value]
               ) extends Identity

object User {
  implicit val formatterUser = Json.format[User]
}

