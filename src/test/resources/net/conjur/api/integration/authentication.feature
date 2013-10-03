Feature: Conjur authentication
  Scenario: Fetch an api token
    Given valid credentials
    Then I can fetch an api token

  Scenario: Fetch an api key
    Given valid credentials
    Then I can fetch an api key

  Scenario: Invalid credentials cause authenticate to fail
    Given bogus credentials
    When I try to authenticate
    Then it throws not authorized

  Scenario: Invalid credentials cause login to fail
    Given bogus credentials
    When I try to login
    Then it throws not authorized

  Scenario: Fetch a token and check its expiration
    Given valid credentials
    Then I can fetch an api token
    And I show the token