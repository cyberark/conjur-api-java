package net.conjur.api.specs.support

import net.conjur.api.Credentials
import net.conjur.api.authn.{CachingAuthnProvider, AuthnProvider, AuthnClient}
trait WorldLike {
  def adminCredentials : Credentials
  def adminAuthnClient : AuthnProvider
  def adminAuthn : AuthnProvider

  def authnClient : AuthnProvider
}



object World {

}
