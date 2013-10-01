Feature: Client creates variables
  Scenario: Create and retrieve variables as admin
    Given I am an admin
    When I create a variable "foo" with value "bar"
    And I get the value of variable "foo"
    Then the value should be "bar"


  Scenario: Create and retrieve variables as user
    Given a user named Alice
    And I am logged in as Alice
    When I create a variable "foo" with value "bar"
    And I get the value of variable "foo"
    Then the value should be "bar"

  Scenario: Access values by version
    Given I am an admin
    When I create a variable with id "foo"
    Then it should have 0 versions
    When I add a value "blah"
    And I add a value "fizz"
    Then it should have 2 versions
    And the value of version 0 is "blah"
    And the value of version 1 is "fizz"
