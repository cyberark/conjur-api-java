package net.conjur.api.integration

import org.scalatest.{ShouldMatchers, FlatSpec}
import net.conjur.api.authn.{Token, AuthnProvider, CachingAuthnProvider, AuthnClient}
import net.conjur.api.Credentials
import org.scalatest.mock.MockitoSugar
import  org.mockito.Mockito._
import org.mockito.Matchers._
import net.conjur.api.specs.support.{AuthnClientBehaviors, AuthnProviderBehaviors, Tardis}
import scala.collection.mutable

trait HasAuthnClients {
  def admin = new AuthnClient(Credentials.fromSystemProperties())
  def bogus = new AuthnClient("bogus", "shmufogus")
}

/**
 *
 */
class AuthnClientTest extends FlatSpec with ShouldMatchers
    with AuthnProviderBehaviors with AuthnClientBehaviors with HasAuthnClients{

  "An AuthnClient with admin credentials" should behave like canAuthenticate(admin)
  it should behave like canLogin(admin)

  "An AuthnClient with bogus credentials" should behave like cannotLogin(bogus)
  it should behave like canNotAuthenticate(bogus)
}


class CachingAuthnProviderTest extends FlatSpec with ShouldMatchers
  with AuthnProviderBehaviors with HasAuthnClients with Tardis {
  lazy val caching = new CachingAuthnProvider(admin)

  "A CachingAuthnProvider" should behave like cachesTokens(caching)

  it should "request a new token if the current one expires" in {
    trait AuthnCall
    case object NoArgs extends AuthnCall
    case class WithArg(val arg:Boolean) extends AuthnCall

    val mockAuthn = new AuthnProvider {
      var calls = 0
      def authenticate(useCachedToken: Boolean): Token = authenticate
      def authenticate(): Token = {
        calls += 1
        admin.authenticate
      }
    }

    val authn = new CachingAuthnProvider(mockAuthn)
    val firstToken = authn.authenticate

    timeTravel(firstToken.getExpiration.plusMinutes(5)) { () =>
      firstToken.isExpired should be(true)
      val secondToken = authn.authenticate
      mockAuthn.calls should equal(2)
    }
  }
}