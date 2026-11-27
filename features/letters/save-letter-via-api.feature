# Status: proposed (AI-assisted, awaiting review). Bridge to the tests:
# ApiSaveLetterTest in the module adapter-api, one test per scenario, named
# after the scenario title. There the use case is a fake; its rules are checked
# by save-letter.feature. The transaction manager is a real
# AbstractPlatformTransactionManager without a database.
Feature: Save letter via the Java API
  A host saves a letter in its own use case, for example when completing a task,
  and commits its own changes together with the letter (ADR-023). It receives the
  result as a return value and sees neither the domain model nor the internal
  ports of the library. The rules from save-letter.feature apply: the same
  authorization check and at most one letter per snapshot (ADR-021).

  Background:
    Given Erika has created a preview of the "Payment reminder" in version 2 for customer 4711

  Scenario: The host receives the identifiers of the saved letter
    When the host saves the preview for Erika via the API
    Then the letter is saved
    And the result names the ID of the letter, customer 4711, the "Payment reminder" in version 2, Erika and the time of creation

  Scenario: A repetition receives the already saved letter
    Given the letter from the preview is already saved
    When the host saves the preview for Erika again via the API
    Then it receives the already saved letter with the same identifiers

  Scenario Outline: A business rejection comes back as result
    Given the use case rejects the saving: <rejection>
    When the host saves the preview for Erika via the API
    Then it receives the rejection <rejection>

    Examples:
      | rejection          |
      | SnapshotUnknown    |
      | SnapshotExpired    |
      | NotAllowed         |
      | DataContextChanged |
      | NotConformant      |

  Scenario: Saving joins the host's transaction
    Given the host has opened a transaction
    When the host saves the preview for Erika via the API
    Then the saving runs in the host's transaction
    And no further transaction was opened

  Scenario: Without a host transaction, saving opens its own
    When the host saves the preview for Erika via the API without a transaction of its own
    Then the saving ran in a transaction of its own, which was committed

  Scenario: A business rejection lets the host's transaction commit
    Given the host has opened a transaction
    And the use case rejects the saving: NotAllowed
    When the host saves the preview for Erika via the API
    And the host commits its transaction
    Then the host's transaction is committed and not rolled back

  Scenario: A technical error rolls back the host's transaction
    Given the host has opened a transaction
    And the use case fails with a technical error
    When the host saves the preview for Erika via the API
    Then the host receives the error
    And the host's transaction can only be rolled back

  Scenario: An invalid user ID rolls back the host's transaction
    Given the host has opened a transaction
    When the host saves the preview with an empty user ID via the API
    Then the host receives a caller error
    And the use case was not invoked
    And the host's transaction can only be rolled back

  Scenario: If the host rolls back, saving rolls back too
    Given the host has opened a transaction
    When the host saves the preview for Erika via the API
    And the host rolls back its transaction
    Then nothing is committed
