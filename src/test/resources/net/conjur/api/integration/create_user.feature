Feature: Creating users
  Scenario: Create a user
    When I create a user named alice
    Then it should have worked

  Scenario: Log in as a created user
    When I create a user named Bob
    Then it should have worked
    When I am logged in as Bob
    Then I can retrieve a token
