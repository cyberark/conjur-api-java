package net.conjur.api.specs

import java.net.URI

import net.conjur.api.Endpoints
import org.scalatest.{GivenWhenThen, FunSpec, ShouldMatchers}

class EndpointsTest extends FunSpec with GivenWhenThen with ShouldMatchers {
  object EndpointStrings {
    def create(applianceUri : String) = EndpointStrings(
      applianceUri + "/authn",
      applianceUri + "/authz",
      applianceUri
    )

    def fromEndpoints(endpoints: Endpoints) = EndpointStrings(
      endpoints.getAuthnUri.toString,
      endpoints.getAuthzUri.toString,
      endpoints.getDirectoryUri.toString
    )
  }
  case class EndpointStrings(authnUriString : String, authzUriString : String,
                              directoryUriString : String){
    lazy val authnUri = URI create authnUriString
    lazy val authzUri = URI create authzUriString
    lazy val directoryUri = URI create directoryUriString
  }

  describe("Creating Endpoints from an appliance uri"){
    val validApplianceUriString = "https://conjur.companyname.com/api"
    val validApplianceUri = URI create validApplianceUriString

    val expected = EndpointStrings.create(validApplianceUriString)

    it("handles a valid appliance uri string correctly"){
      Given(s"A valid appliance uri string $validApplianceUriString")
      When("I create Endpoints with Endpoints.getApplianceEndpoints from the appliance uri")
      val endpoints = Endpoints getApplianceEndpoints validApplianceUriString
      Then("The endpoints should be correct")
      EndpointStrings.fromEndpoints(endpoints) should equal(expected)
    }

    it("handles a valid appliance uri URI correctly"){
      Given(s"A valid appliance URI of $validApplianceUri")
      When("I create Endpoints with the URI")
      val endpoints = Endpoints getApplianceEndpoints validApplianceUri
      Then("The endpoints should be correct")
      EndpointStrings.fromEndpoints(endpoints) should equal(expected)
    }
  }

  describe("Handling invalid input to getApplianceEndpoints"){
    val notHttps = "http://foo.bar.com/api"
    val noApiPath = "https://foo.bar.com"
    val noScheme = "foo.bar.com/api"

    it("fails when the scheme is not https"){
      intercept[IllegalArgumentException] {
        Endpoints getApplianceEndpoints notHttps
        fail("It didn't fail!")
      }
    }

    it("fails when the scheme is missing"){
      intercept[IllegalArgumentException]{
        Endpoints getApplianceEndpoints noScheme
        fail("It didn't fail!")
      }
    }

    it("fails when the path isn't /api"){
      intercept[IllegalArgumentException]{
        Endpoints getApplianceEndpoints noApiPath
        fail("It didn't fail!")
      }
    }
  }


}
