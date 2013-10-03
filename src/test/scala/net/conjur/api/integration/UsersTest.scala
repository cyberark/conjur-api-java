package net.conjur.api.integration

import net.conjur.api.{Credentials, Conjur}
import org.scalatest.{ShouldMatchers, FlatSpec}
import net.conjur.api.specs.support.{RandomId, ResourceAuthnBehaviors}

/**
 *
 */
class UsersTest extends FlatSpec with ShouldMatchers with ResourceAuthnBehaviors {
  lazy val adminCredentials = Credentials.fromSystemProperties
  lazy val admin = new Conjur(adminCredentials)
  lazy val userName = new RandomId("java-api-test-user", 10)

  def newUserClient() = {
    val user= admin.users.create(userName ++)
    new Conjur(user.getLogin, user.getApiKey)
  }

  "The admin client" should behave like resourceWithValidAuthn(admin)

  "An admin" should "be able to create a user" in {
    val user = admin.users.create(userName ++)
  }

  "A created user" should "be able to authenticate" in {
    val client = newUserClient
    client.getAuthn.authenticate
  }
}
