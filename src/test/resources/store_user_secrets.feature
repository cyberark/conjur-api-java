Feature: Conjur stores user secrets
  Background:
    Given I am an admin
    Then I create a user named Alice
    And I create a user named Bob
    And Alice has a variable named "alice-secret" holding "super secret!"

  Scenario: Alice can access her secrets
    Given I am logged in as Alice
    When I get the value of variable "alice-secret"
    Then the value should be "super secret!"

  Scenario: Bob cannot access Alice's secrets
    Given I am logged in as Bob
    When I try to get the value of variable "alice-secret"
    Then permission is denied


