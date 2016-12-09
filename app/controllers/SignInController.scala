package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.api.{LoginEvent, Silhouette}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.SignInForm
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.UserService
import utils.auth.DefaultEnv

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * The `Sign In` controller.
  *
  * @param silhouette          The Silhouette stack.
  * @param userService         The user service implementation.
  * @param authInfoRepository  The auth info repository implementation.
  * @param credentialsProvider The credentials provider.
  * @param configuration       The Play configuration.
  * @param clock               The clock instance.
  */
class SignInController @Inject()(
                                  silhouette: Silhouette[DefaultEnv],
                                  userService: UserService,
                                  authInfoRepository: AuthInfoRepository,
                                  credentialsProvider: CredentialsProvider,
                                  configuration: Configuration,
                                  clock: Clock)
                                (implicit exec: ExecutionContext)
  extends Controller {

  /**
    * Handles the submitted JSON data.
    *
    * @return The result to display.
    */
  def submit = Action.async(parse.json) { implicit request =>
    request.body.validate[SignInForm.Data].map { data =>
      credentialsProvider.authenticate(Credentials(data.email, data.password)).flatMap { loginInfo =>
        userService.retrieve(loginInfo).flatMap {
          case Some(user) => silhouette.env.authenticatorService.create(loginInfo).map {
            case authenticator if data.rememberMe =>
              val c = configuration.underlying
              authenticator.copy(
                expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
              )
            case authenticator => authenticator
          }.flatMap { authenticator =>
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            silhouette.env.authenticatorService.init(authenticator).map { token =>
              Ok(Json.obj("token" -> token))
            }
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }.recover {
        case e: ProviderException =>
          Unauthorized(Json.obj("message" -> "invalid credentials"))
      }
    }.recoverTotal(error =>
      Future.successful(Unauthorized(Json.obj("message" -> "invalid credentials"))))
  }

}
