Feature: User operations
  Scenario: Create a user as admin
    Given I am an admin
    Then I can create a user named Alice

  Scenario: Log in as a created user
    Given I am an admin
    And I create a user named Bob
    Then I can login as the user

  Scenario: Check for created user existence
    Given I am an admin
    And I create a user named Bob
    Then user Bob exists

  Scenario: Check for non-existent user existence
    Given I am an admin
    Then user Santa does not exist

  Scenario: Create a user with a password and log in using the password
    Given I am an admin
    When I create a user with a password
    Then I can authenticate with the password

  Scenario: User can change her password
    Given I am an admin
    And I create a user with a password
    When I authenticate with the password
    And I change the user's password
    Then I can authenticate with the password
