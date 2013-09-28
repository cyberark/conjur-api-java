package net.conjur.api.authn

import org.scalatest.{ShouldMatchers, GivenWhenThen, FunSpec}

/**
 *
 */
class TokenSpec extends FunSpec with GivenWhenThen with ShouldMatchers{
  describe("A Token") {
    it("decodes json properly") {
      Given("some json")
      val jsonString = """{"data":"some data", "key":"some key", "signature":"a signature", """
    }

    it("encodes base64 properly") {

    }

    it("generates the header") {

    }
  }
}
