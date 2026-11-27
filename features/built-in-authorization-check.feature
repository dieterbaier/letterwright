# Status: proposed (AI-assisted, awaiting review). Bridge to the tests:
# BuiltInAuthorizationCheckTest in the module spring-boot-starter, one test per
# scenario, named after the scenario title.
Feature: Built-in authorization check
  If the host application provides no authorization check of its own, the library
  decides based on the roles from the host's Spring Security. Each action needs
  its own role, for letters by default ROLE_LETTERWRIGHT_LETTERS_<ACTION>.

  Background:
    Given the host application provides no authorization check of its own

  Scenario: With the role of the action, it is allowed
    Given Erika is logged in and has the role "ROLE_LETTERWRIGHT_LETTERS_CREATE"
    When the library asks whether Erika may create a letter for customer 4711
    Then it is allowed

  Scenario: With the role of another action, it is not allowed
    Given Erika is logged in and has only the role "ROLE_LETTERWRIGHT_LETTERS_VIEW"
    When the library asks whether Erika may create a letter for customer 4711
    Then it is not allowed

  Scenario Outline: Without authentication, nothing is allowed
    Given the host has configured the role "ROLE_ANONYMOUS" for creating letters
    And <access>
    When the library asks whether Erika may create a letter for customer 4711
    Then it is not allowed

    Examples:
      | access                                                          |
      | nobody is logged in                                             |
      | Erika accesses only anonymously, with the role "ROLE_ANONYMOUS" |

  Scenario: The host decides what counts as anonymous access
    Given the host classifies every authentication as anonymous with its own AuthenticationTrustResolver
    And Erika is logged in and has the role "ROLE_LETTERWRIGHT_LETTERS_CREATE"
    When the library asks whether Erika may create a letter for customer 4711
    Then it is not allowed

  Scenario: Nothing is allowed for a user other than the logged-in one
    Given Max is logged in and has the role "ROLE_LETTERWRIGHT_LETTERS_CREATE"
    When the library asks whether Erika may create a letter for customer 4711
    Then it is not allowed

  Scenario: A configured role replaces the predefined one
    Given the host has configured the role "ROLE_CASEWORK" for creating letters
    And Erika is logged in and has only the role "ROLE_LETTERWRIGHT_LETTERS_CREATE"
    When the library asks whether Erika may create a letter for customer 4711
    Then it is not allowed

  Scenario: A custom authorization check of the host replaces the built-in one
    Given the host application does provide an authorization check of its own that allows everything
    And nobody is logged in
    When the library asks whether Erika may create a letter for customer 4711
    Then it is allowed
