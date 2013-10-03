Feature: Variable operations
  Background:
    Given a user named "Alice"
    And I login as the user

  Scenario: Trying to access a non-existent variable
    When I try to get the value of "does not exist"
    Then it throws not found

  Scenario: Trying to access someone else's variable
    Given I create a variable "alice-secret"
    And a user named "Bob"
    And I log in as the user
    When I try to get the value
    Then it throws not found

  Scenario: Store user secrets in a variable
    When I create a variable "alice-secret"
    And I add the value "super secret"
    When I get the value
    Then the value should be "super secret"

  Scenario: Variables are versioned
    When I create a variable "versioned-variable"
    Then it should have 0 versions
    When I add the value "first"
    Then it should have 1 version
    When I add the value "second"
    Then it should have 2 versions
    When I get the value
    Then the value should be "second"
    When I get the value of version 1
    Then the value should be "first"
    When I get the value of version 2
    Then the value should be "second"
    When I try to get the value of version 42
    Then it throws not found

  Scenario: Check for created variable's existence
    Given I create a variable "foo"
    Then variable "foo" should exist

  Scenario: Variables not created do not exist
    Then variable "santa clause" should not exist
