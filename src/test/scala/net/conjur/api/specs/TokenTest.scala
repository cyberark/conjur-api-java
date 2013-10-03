package net.conjur.api.specs

import org.scalatest.{ShouldMatchers, GivenWhenThen, FunSpec}
import net.conjur.api.authn.Token
import org.joda.time.{DateTime, DateTimeZone}
import net.conjur.api.specs.support.Tardis

/**
 *
 */
class TokenTest extends FunSpec with GivenWhenThen with ShouldMatchers with Tardis {
  describe("Timestamp handling"){
    it("Parses a date correctly"){
      Given("a date string '2013-10-01 18:48:32 UTC'")
      val dateString = "2013-10-01 18:48:32 UTC"
      When("Token.DATE_TIME_FORMATTER parses it")
      val date = Token.DATE_TIME_FORMATTER.parseDateTime(dateString)
      Then("the date has the correct fields")
      date.getYearOfCentury should equal(13)
      date.getMonthOfYear should equal(10)
      date.getDayOfMonth should equal(1)
      date.getHourOfDay should equal(18)
      date.getMinuteOfHour should equal(48)
      date.getSecondOfMinute should equal(32)
      date.getZone should equal(DateTimeZone.UTC)
    }

    it("Has the correct expiration and helpers"){
      Given("a DateTime 5 minutes from now")
      val then = DateTime.now().plusMinutes(5)
      And("token json containing the date time")
      val tokenJson = "{ \"timestamp\":\"" ++ Token.DATE_TIME_FORMATTER.print(then) ++ "\"}"
      When("I create a token from the json")
      val token = Token.fromJson(tokenJson)
      Then("its timestamp should be the date")
      // TODO write a custom matcher for this
      token.getTimestamp.withMillisOfSecond(0) should equal(then.withMillisOfSecond(0))
      And("its expiration should be 8 minutes after the date")
      token.getExpiration.withMillisOfSecond(0) should equal(then.plusMinutes(8).withMillisOfSecond(0))
      And("it will expire within 15 minutes")
      token.willExpireWithin(15 * 60) should equal(true)
      And("it will not expire within 1 minutes")
      token.willExpireWithin(60) should equal(false)
      When("I get in my TARDIS and go 15 minutes into the future")
      timeTravel(DateTime.now.plusMinutes(15)){ ()=>
        Then("it should have expired")
        token.isExpired should equal(true)
      }

    }
  }
}
