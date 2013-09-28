package net.conjur.api

import org.scalatest.{ShouldMatchers, GivenWhenThen, FunSpec}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EndpointsSpec extends FunSpec with GivenWhenThen
                                    with ShouldMatchers {
  val account = "the-account"
  val stack   = "the-stack"
  val production = new Endpoints("production", account, stack)
  val development = new Endpoints("development", account, stack)

  it("Endpoints should use localhost in development") {
    Given("Endpoints('development', 'v4', 'acct')")
    val endpoints = new Endpoints("development", "acct", "v4")

    Then("authz is http://localhost:5100")
    endpoints.authz() should equal ("http://localhost:5100")

    And("authn is http://localhost:5000")
    endpoints.authn() should equal ("http://localhost:5000")

    And("directory is http://localhost:5200")
    endpoints.directory() should equal ("http://localhost:5200")
  }

  it("Endpoints should have the right urls in production") {
    Given("Endpoints('production', 'v4', 'acct')")
    val endpoints = new Endpoints("production", "v4", "acct")

    Then("authz points to https://authz-v4-conjur.herokuapp.com")
    endpoints.authz() should equal("https://authz-v4-conjur.herokuapp.com")

    And("authn points to https://authn-acct-conjur.herokuapp.com")
    endpoints.authn() should equal("https://authn-acct-conjur.herokuapp.com")

    And("directory points to https://core-acct-conjur.herokuapp.com")
    endpoints.directory() should equal("https://core-acct-conjur.herokuapp.com")


  }
}
