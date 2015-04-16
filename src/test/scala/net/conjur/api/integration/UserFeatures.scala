package net.conjur.api.integration
import scala.util.Random
import org.scalatest._
import net.conjur.api.authn._
import net.conjur.api._
import System.out.println

case class UserWithPassword(user:User, password:String){
  def this(user:User) = this(user, user.getApiKey)
  def login = user.getLogin
}
object UserWithPassword{
  def apply(user:User):UserWithPassword = new UserWithPassword(user)
  implicit def userToUserWithPassword(user:User):UserWithPassword = UserWithPassword(user)
  implicit def userWithPasswordToCredentials(uwp:UserWithPassword):Credentials = new Credentials(uwp.user.getLogin, uwp.password)
}

/**
 *
 */
class UserFeatures extends FeatureSpec
    with ShouldMatchers
    with ConjurFixtures
    with GivenWhenThen
    with IsIntegrationFeature
    {

  info("As a Conjur based service")
  info("I want to create Conjur users")
  info("in order to represent my own users")
  feature("User creation"){
    scenario("Create and authenticate as a user without a password"){
      When("I create a user without a password")
      val alice = admin.users.create(ns("alice"))

      def authn = new AuthnClient(alice.getLogin, alice.getApiKey)
      Then("I can authenticate with the user's api key")
      authn.authenticate

      And("I can login with her api key")
      authn.login should equal(alice.getApiKey)
    }

    scenario("Create and authenticate as a user with a password"){
      When("I create a user with a password")
      val pw = randomPassword
      val alice = admin.users.create(ns("alice"), pw)
      def authn(auth:String) = new AuthnClient(alice.getLogin, auth)

      Then("I can authenticate with her api key")
      authn(alice.getApiKey).authenticate

      And("I can authenticate with her password")
      authn(pw).authenticate

      And("I can login with her api key")
      authn(alice.getApiKey).login

      And("I can log in with her password")
      authn(pw).login

    }
  }

  info("As a conjur based service")
  info("I want to check whether a user exists")
  info("in order to tell users if a username is available")
  feature("User existence testing"){
    scenario("Created users exist"){
      Given("a user I created")
      val alice = admin.users.create(ns("alice"))

      Then("the user exists")
      admin.users.exists(alice.getLogin) should be(true)
    }

    scenario("The admin user exists"){
      Then("the user admin exists")
      admin.users.exists(admin.getUsername) should be(true)
    }

    scenario("A random username should not exist"){
      Given("A random username")
      val username = randomUsername
      Then("no user exists with the name")
      admin.users.exists(username) should be(false)
    }
  }

  feature("API key access"){
    scenario("Exchange a password for an API key"){
      Given("A user named alice with a random password")
      val pw = randomPassword
      val alice = admin.users.create(ns('alice), pw)

      Then("I can get her api key using her password")
      new AuthnClient(alice.getLogin, pw).login should equal(alice.getApiKey)
    }
  }

  feature("Password updates"){
    scenario("Change a user's password using her old password"){
      Given("A user with a password")
      val password = randomPassword
      val user = admin.users.create(randomUsername, password)

      When("I authenticate using her username and original password")
      val client = new AuthnClient(user.getLogin, password)

      And("I generate a new random password")
      val newPassword = randomPassword

      And("I update her password to the new password")
      client.updatePassword(newPassword)

      Then("I can authenticate using the new password")
      new AuthnClient(user.getLogin, newPassword).authenticate

      And("I can login using the new password")
      new AuthnClient(user.getLogin, newPassword).login
    }
    scenario("Change a user's password using her api key"){
      Given("a user named alice")
      val alice = loginAs('alice)

      And("a random password")
      val password = randomPassword

      When("I update her password to the new password")
      new AuthnClient(alice.getUsername, alice.getPassword).updatePassword(password)

      Then("I can authenticate with the new password")
      new AuthnClient(alice.getUsername, password).authenticate
    }
  }






}
