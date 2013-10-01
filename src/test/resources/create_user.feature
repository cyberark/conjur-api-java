Feature: Creating users
  Scenario: Create a user as admin
    When
    When I create a user named alice
    Then it should have worked

  Scenario: Log in as a created user
    When I create a user named Bob
    Then it should have worked
