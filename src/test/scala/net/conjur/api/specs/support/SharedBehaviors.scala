package net.conjur.api.specs.support

import org.scalatest.{ShouldMatchers, FlatSpec, FunSpec, GivenWhenThen}
import net.conjur.api.authn.{AuthnClient, AuthnProvider}
import net.conjur.api.{Resource, Credentials}
import javax.ws.rs.NotAuthorizedException

/**
 *
 */
trait AuthnProviderBehaviors extends ShouldMatchers { this: FlatSpec =>
  def canAuthenticate(newAuthn: => AuthnProvider){
    it should "authenticate successfully" in {
      newAuthn.authenticate
    }
    it should "not cache tokens when asked not to" in {
      val authn = newAuthn
      val first = authn.authenticate
      authn.authenticate(false).getTimestamp shouldNot equal(first.getExpiration)
    }
  }

  def canNotAuthenticate(newAuthn: => AuthnProvider){
    it should "not authenticate successfully" in {
      evaluating{newAuthn.authenticate} should produce [NotAuthorizedException]
    }
  }

  def cachesTokens(newAuthn: => AuthnProvider) {
    it should "cache tokens when asked to" in {
      val authn = newAuthn
      val first = authn.authenticate(true)
      first.getTimestamp should equal(authn.authenticate(true).getTimestamp)
    }
  }

}

trait AuthnClientBehaviors extends ShouldMatchers {this: FlatSpec =>
  def canLogin(authn: => AuthnClient) {
    it should "be able to login" in {
      authn.login
    }
  }
  def cannotLogin(authn:AuthnClient) {
    it should "not be able to login" in {
      evaluating { authn.login } should produce [NotAuthorizedException]
    }
  }
}

trait ResourceAuthnBehaviors extends ShouldMatchers with AuthnProviderBehaviors {this:FlatSpec =>
  def resourceWithValidAuthn(restResource: => Resource) {
    "its authn provider" should behave like canAuthenticate(restResource.getAuthn)
  }

  def resourceWithInvalidAuthn(restResource: => Resource) {
    "its authn provider" should behave like canNotAuthenticate(restResource.getAuthn)
  }
}