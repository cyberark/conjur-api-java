package net.conjur.api.integration

import net.conjur.api.{Credentials, Conjur}
import org.scalatest.{GivenWhenThen, ShouldMatchers, FlatSpec}
import net.conjur.api.specs.support.{RandomId, ResourceAuthnBehaviors}
import scala.util.Random
import net.conjur.api.authn.AuthnClient

/**
 *
 */
class UserFeatures extends FlatSpec
    with ShouldMatchers
    with ResourceAuthnBehaviors
    with ConjurFixtures
    with GivenWhenThen {

  lazy val userName = new RandomId("java-api-test-user", 10)

  def newUserClient() = {
    val user= admin.users.create(userName ++)
    new Conjur(user.getLogin, user.getApiKey)
  }

  "The admin client" should behave like resourceWithValidAuthn(admin)

  "An admin" should "be able to create a user" in {
    val user = admin.users.create(userName ++)
  }

  "A client for a created user" should "be able to authenticate" in {
    val client = newUserClient
    client.getAuthn.authenticate
  }

  "A created user" should "have an apiKey" in {
    admin.users.create(userName ++).getApiKey should not be(null)
  }

  "A user" should "be able to change her password and login with the new one" in {
    Given("a random password")
    val password = Random.nextString(10)

    When("I create a user with the password")
    val user = admin.users.create(userName ++, password)

    And("I create an authn client with the username and password")
    val authn = new AuthnClient(user.getLogin, password)

    Then("the authn client can authenticate with the old password")
    authn.authenticate

    And("it can change the user's password")
    val newPassword = Random.nextString(10)
    authn.updatePassword(newPassword)

    When("I create an authn client with the new password")
    val newAuthn = new AuthnClient(user.getLogin, newPassword)

    Then("it can authenticate")
    newAuthn.authenticate
  }


}
