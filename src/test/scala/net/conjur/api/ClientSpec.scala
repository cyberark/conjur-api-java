package net.conjur.api

import org.scalatest.{MustMatchers, ShouldMatchers, GivenWhenThen, FunSpec}
import java.net.URI
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._

import org.apache.http.client.fluent.{Response, Request}
import java.io.IOException
import net.conjur.api.exceptions.ConjurApiException
import org.apache.http.client.HttpResponseException
import net.conjur.api.exceptions.http.NotFoundException
import org.mockito.stubbing.OngoingStubbing


/**
 *
 */
class ClientSpec extends FunSpec with GivenWhenThen with MustMatchers with MockitoSugar {

  val uri = URI.create("http://example.com")

  class TestClient extends Client(new Endpoints("production", "v4", "acct")) {
    def getUri = uri
    override def prepareRequest(r:Request) = super.prepareRequest(r)
  }

  val client = new TestClient
  describe("A Client"){
  it("#getUri(path) works as expected"){
    When("I call getUri with 'foo'")
    val a = client.getUri("foo")
    Then("the uri returned has path '/foo'")
    a.toString() must equal("http://example.com/foo")
    a.getPath() must equal("/foo")

    When("I add a leading slash")
    val b = client.getUri("/foo")
    Then("I get the same result")
    b must equal(a)

    When("I add a trailing slash")
    val c = client.getUri("foo/")
    Then("I get the same result")
    c must equal(a)

    When("I give an empty string")
    val d = client.getUri("")
    Then("nothing is appended")
    d must equal(uri)
  }


  it("#response wraps unchecked exceptions thrown by request.execute") {
    Given("a request that throws an exception when execute is called")
    val request = mock[Request]

    When("it throws an IOException")
    stub(request.execute()) toThrow (new IOException())

    And("client.response throws a ConjurApiException")
    evaluating{ client.response(request) } must produce[ConjurApiException]

    When("the exception is an HttpResponseException with status code 404")
    reset(request)
    stub(request.execute()) toThrow (new HttpResponseException(404, "Not found"))

    Then("client.response throws a NotFoundException")
    evaluating{ client.response(request) } must produce[NotFoundException]
  }

  it("#responseString wraps unchecked exceptions thrown by response.returnContent()") {
    Given("a mocked response that throws when returnContent is called")
    val response = mock[Response]
    And("a request that returns it from execute()")
    val request = mock[Request]
    stub(request.execute()) toReturn(response)

    When("response.returnContent throws an IOException")
    stub(response.returnContent()) toThrow(new IOException())

    Then("client.responseString(request) throws a ConjurApiException")
    evaluating(new TestClient().responseString(request)) must produce[ConjurApiException]
  }

  it("#response calls prepareRequest before passing a request to execute"){
    Given("a Request")
    val request = mock[Request]
    And("a client")
    val client = new TestClient
    val clientSpy = spy(client)

    When("I pass the request to client.execute")
    clientSpy.response(request)

    Then("client.prepareRequest should have been called with the request")
    verify(clientSpy).prepareRequest(request)
  }
  }
}
