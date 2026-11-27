# Status: proposed (AI-assisted, awaiting review). Bridge to the tests:
# DeleteLetterServiceTest in the module usecases-service, one test per scenario,
# named after the scenario title. There the ports are replaced by fakes.
Feature: Delete letter
  A clerk deletes a saved letter. Whether and how it is deleted is decided by the
  host's deletion policy (RB-10, ADR-007): hard together with the PDF, logically
  while keeping the PDF, or not at all. The library asks it only after the host has
  allowed the deletion. A letter the user may not delete does not exist for them
  (QS-009).

  Background:
    Given a letter is saved for customer 4711
    And Erika may delete the letters for customer 4711

  Scenario: Hard deleted, letter and PDF are removed
    Given the deletion policy decides "hard"
    When Erika deletes the letter
    Then the letter is deleted
    And the letter is removed
    And the PDF is removed

  Scenario: Logically deleted, the PDF stays, the letter is no longer visible
    Given the deletion policy decides "logical"
    When Erika deletes the letter
    Then the letter is deleted
    And the letter is saved as logically deleted
    And the PDF is still stored
    And the letter no longer appears in the list

  Scenario: A blocked deletion names the reason
    Given the deletion policy blocks with the reason "Retention period until 2036"
    When Erika deletes the letter
    Then the deletion is blocked with the reason "Retention period until 2036"
    And the letter is unchanged

  Scenario: A letter that is not allowed behaves like a missing one
    Given Max may not delete the letters for customer 4711
    When Max deletes the letter
    Then the letter is unknown
    And the deletion policy was not asked
    And the letter is unchanged

  Scenario: An unknown letter does not exist
    When Erika deletes a letter with an unknown ID
    Then the letter is unknown
    And the host was not asked

  Scenario: A logically deleted letter is not deleted again
    Given the letter is logically deleted
    When Erika deletes the letter
    Then the letter is unknown
    And the host was not asked
