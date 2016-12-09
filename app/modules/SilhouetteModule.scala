package modules

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorService, JWTAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.oauth2.state.DummyStateProvider
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, PlayCacheLayer, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.{DelegableAuthInfoDAO, InMemoryAuthInfoDAO}
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.libs.concurrent.Execution.Implicits._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import service.{UserService, UserServiceImpl}
import utils.auth.DefaultEnv



class SilhouetteModule extends AbstractModule with ScalaModule {
  /**
    * Configures the module.
    */
  def configure() {
    bind[UserService].to[UserServiceImpl]
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[OAuth2StateProvider].to[DummyStateProvider]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())

    // Replace this with the bindings to your concrete DAOs
    bind[DelegableAuthInfoDAO[PasswordInfo]].toInstance(new InMemoryAuthInfoDAO[PasswordInfo])
    bind[DelegableAuthInfoDAO[OAuth1Info]].toInstance(new InMemoryAuthInfoDAO[OAuth1Info])
    bind[DelegableAuthInfoDAO[OAuth2Info]].toInstance(new InMemoryAuthInfoDAO[OAuth2Info])
    bind[DelegableAuthInfoDAO[OpenIDInfo]].toInstance(new InMemoryAuthInfoDAO[OpenIDInfo])
  }

  /**
    * Provides the HTTP layer implementation.
    *
    * @param client Play's WS client.
    * @return The HTTP layer implementation.
    */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
    * Provides the Silhouette environment.
    *
    * @param userService          The user service implementation.
    * @param authenticatorService The authentication service implementation.
    * @param eventBus             The event bus instance.
    * @return The Silhouette environment.
    */
  @Provides
  def provideEnvironment(
                          userService: UserService,
                          authenticatorService: AuthenticatorService[JWTAuthenticator],
                          eventBus: EventBus): Environment[DefaultEnv] = {

    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  /**
    * Provides the crypter for the OAuth1 token secret provider.
    *
    * @param configuration The Play configuration.
    * @return The crypter for the OAuth1 token secret provider.
    */
  @Provides
  @Named("oauth1-token-secret-crypter")
  def provideOAuth1TokenSecretCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.oauth1TokenSecretProvider.crypter")

    new JcaCrypter(config)
  }

  /**
    * Provides the crypter for the authenticator.
    *
    * @param configuration The Play configuration.
    * @return The crypter for the authenticator.
    */
  @Provides
  @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  /**
    * Provides the auth info repository.
    *
    * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
    * @param oauth1InfoDAO   The implementation of the delegable OAuth1 auth info DAO.
    * @param oauth2InfoDAO   The implementation of the delegable OAuth2 auth info DAO.
    * @param openIDInfoDAO   The implementation of the delegable OpenID auth info DAO.
    * @return The auth info repository instance.
    */
  @Provides
  def provideAuthInfoRepository(
                                 passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
                                 oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
                                 oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info],
                                 openIDInfoDAO: DelegableAuthInfoDAO[OpenIDInfo]): AuthInfoRepository = {

    new DelegableAuthInfoRepository(passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO, openIDInfoDAO)
  }

  /**
    * Provides the authenticator service.
    *
    * @param crypter       The crypter implementation.
    * @param idGenerator   The ID generator implementation.
    * @param configuration The Play configuration.
    * @param clock         The clock instance.
    * @return The authenticator service.
    */
  @Provides
  def provideAuthenticatorService(
                                   @Named("authenticator-crypter") crypter: Crypter,
                                   idGenerator: IDGenerator,
                                   configuration: Configuration,
                                   clock: Clock): AuthenticatorService[JWTAuthenticator] = {

    val config = configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.authenticator")
    val encoder = new CrypterAuthenticatorEncoder(crypter)

    new JWTAuthenticatorService(config, None, encoder, idGenerator, clock)
  }

//  /**
//    * Provides the avatar service.
//    *
//    * @param httpLayer The HTTP layer implementation.
//    * @return The avatar service implementation.
//    */
//  @Provides
//  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)

  //  /**
  //    * Provides the OAuth1 token secret provider.
  //    *
  //    * @param cookieSigner The cookie signer implementation.
  //    * @param crypter The crypter implementation.
  //    * @param configuration The Play configuration.
  //    * @param clock The clock instance.
  //    * @return The OAuth1 token secret provider implementation.
  //    */
  //  @Provides
  //  def provideOAuth1TokenSecretProvider(
  //                                        @Named("oauth1-token-secret-cookie-signer") cookieSigner: CookieSigner,
  //                                        @Named("oauth1-token-secret-crypter") crypter: Crypter,
  //                                        configuration: Configuration,
  //                                        clock: Clock): OAuth1TokenSecretProvider = {
  //
  //    val settings = configuration.underlying.as[CookieSecretSettings]("silhouette.oauth1TokenSecretProvider")
  //    new CookieSecretProvider(settings, cookieSigner, crypter, clock)
  //  }

  /**
    * Provides the password hasher registry.
    *
    * @param passwordHasher The default password hasher implementation.
    * @return The password hasher registry.
    */
  @Provides
  def providePasswordHasherRegistry(passwordHasher: PasswordHasher): PasswordHasherRegistry = {
    new PasswordHasherRegistry(passwordHasher)
  }

  /**
    * Provides the credentials provider.
    *
    * @param authInfoRepository     The auth info repository implementation.
    * @param passwordHasherRegistry The password hasher registry.
    * @return The credentials provider.
    */
  @Provides
  def provideCredentialsProvider(
                                  authInfoRepository: AuthInfoRepository,
                                  passwordHasherRegistry: PasswordHasherRegistry): CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }

}
