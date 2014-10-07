package net.conjur.api.integration

import org.scalatest.{GivenWhenThen, ShouldMatchers, FeatureSpec}
import net.conjur.api.Conjur
import javax.ws.rs.ForbiddenException

/**
 *
 */
class VariableFeatures extends FeatureSpec
  with ShouldMatchers
  with GivenWhenThen
  with ConjurFixtures
  with IsIntegrationFeature {

  info("As a Conjur based service")
  info("I want to create variables and modify their values")
  info("So that only an authorized user can retrieve their values")

  feature("A user can read and write values to her variables") {
    scenario("Store a user's secret in a variable and can retrieve the value"){
      Given("a user named alice")
      val alice = loginAs('alice)

      Then("alice can create a variable with a random name to hold her secrets")
      val variable = alice.variables.create("secret", "application/json")

      When("she stores a value in the variable")
      val value = "foobar"
      variable.addValue(value)

      Then("the variable has the right value")
      variable.getValue should equal(value)

      And("the variable has one version")
      variable.getVersionCount should equal(1)

      When("I fetch a variable with the variables id")
      val fetched = alice.variables.get(variable.getId)

      Then("it is non-empty")
      fetched.getVersionCount should equal(1)

      And("it has the right properties")
      fetched.getMimeType should equal("application/json")
      fetched.getKind should equal("secret")
      fetched.getVersionCount should equal(1)

      And("it's value is the value I stored")
      fetched.getValue should equal(value)
    }
  }

  feature("Variable authorization"){
    scenario("alice can access variables that she created"){
      Given("a user alice")
      val alice = loginAs('alice)

      And("the id of a variable that she created")
      val vid = alice.variables.create("alice-variables").getId

      Then("alice can write it's value")
      alice.variables.get(vid).addValue("written")

      And("she can read it's value")
      alice.variables.get(vid).getValue should equal("written")
    }
    scenario("unauthorized users cannot access variables"){
      Given("the id of a variable belonging to alice")
      val secret = loginAs('alice).variables.create("alice-secret")
      secret.addValue("some value")
      val secretId = secret.getId

      And("an evil user bob who wants nothing so much as to steal her secret")
      val bob = loginAs('bob)

      Then("Mean old bob can't read the variable")
      intercept[ForbiddenException] {
        bob.variables.get(secretId).getValue
      }

      And("he certainly can't write the variable!")
      intercept[ForbiddenException] {
        bob.variables.get(secretId).addValue("bob's value")
      }
    }
  }

  feature("Variables are versioned"){
    scenario("I can retrieve an overwritten value for a variable"){
      Given("a variable belonging to 'alice'")
      val alice = loginAs('alice)
      val secret = alice.variables.create("secret")

      And("an original value 'original'")
      val original = "The first and the best, baby!"
      secret addValue original

      When("I overwrite the original")
      val overwritten = "A cheap rip off"
      secret addValue overwritten

      Then("the variables default value is the new one")
      secret.getValue should equal(overwritten)

      And("I can recover the original")
      secret.getValue(1) should equal(original)
    }
  }
}
