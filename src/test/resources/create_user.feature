Feature: Creating users
  Scenario: Create a user as admin
    Given I am an admin
    Then I can create a user named Alice

  Scenario: Log in as a created user
    Given I am an admin
    And I create a user named Bob
    Then I can login as the user
