# Status: proposed (AI-assisted, awaiting review). Bridge to the tests:
# SaveLetterServiceTest in the module usecases-service, one test per scenario,
# named after the scenario title. There the ports are replaced by fakes.
Feature: Save letter
  A clerk saves a checked preview as a letter. Exactly the PDF that is created
  from the snapshot of the preview is saved, and only if it is conformant
  (ADR-005, ADR-007). The library checks the permission again and reads the data
  context again; if it has changed since the preview, a new preview is needed. At
  most one letter is created from a snapshot: saving consumes it and releases its
  data context (ADR-021).

  Background:
    Given Erika created a preview of the "Payment reminder" for customer 4711 10 minutes ago
    And Erika may create a letter from the "Payment reminder" for customer 4711
    And the host still delivers the same data context for customer 4711

  Scenario: An unchanged preview saves the checked PDF
    When Erika saves the preview
    Then a letter is saved
    And its PDF was created from the versions and the data context of the snapshot
    And it carries the checksum of this PDF
    And the snapshot is consumed and no longer holds a data context

  Scenario: If the permission is revoked while saving, nothing is persisted
    Given Erika may no longer create a letter from the "Payment reminder"
    When Erika saves the preview
    Then the saving is not allowed
    And no PDF was created
    And nothing is saved or stored
    And the snapshot is open and keeps its data context

  Scenario: A non-conformant PDF is not saved
    Given the conformance check finds "PDF/UA-1: Image without alternative text"
    When Erika saves the preview
    Then the PDF is not conformant with the finding "PDF/UA-1: Image without alternative text"
    And nothing is saved or stored
    And the snapshot is open and keeps its data context

  Scenario: An uncheckable PDF is not saved
    Given the conformance check cannot check the PDF
    When Erika saves the preview
    Then the saving fails with an error
    And nothing is saved or stored
    And the snapshot is open and keeps its data context

  Scenario: After a change of the data context, no PDF is created
    Given the host now delivers a different data context for customer 4711
    When Erika saves the preview
    Then the data context has changed
    And no PDF was created
    And nothing is saved or stored
    And the snapshot is open and keeps its data context

  Scenario: After a new preview, saving succeeds
    Given the host now delivers a different data context for customer 4711
    And Erika has updated the preview afterwards
    When Erika saves the updated preview
    Then a letter with the new data context is saved

  Scenario: A foreign snapshot behaves like a missing one
    When Max saves the preview of Erika
    Then the snapshot is unknown
    And the host was not asked

  Scenario: A foreign snapshot stays hidden even after saving
    Given Erika has already saved the preview
    When Max saves the preview of Erika
    Then the snapshot is unknown

  Scenario: An expired snapshot is not saved
    Given 30 minutes have passed since the preview
    When Erika saves the preview
    Then the snapshot has expired
    And the host was not asked
    And nothing is saved or stored
    And the snapshot is open and keeps its data context

  Scenario: Repeated saving of the same preview yields no second letter
    Given Erika has already saved the preview but did not receive the response
    When Erika saves the preview again
    Then she receives the already saved letter
    And the host was not asked for the data context again
    And no further PDF was created

  Scenario: If the outcome of saving is uncertain, the PDF is kept
    Given the persistence saves the letter, but its confirmation is lost
    When Erika saves the preview
    Then the saving fails with an error
    And the PDF is still stored
    When Erika saves the preview again
    Then she receives the saved letter with exactly this PDF

  Scenario: If Erika saves while updating the preview, a repetition finds the letter
    Given Erika updates her preview, and the previous snapshot has already been read
    And before it is deleted, another request saves this preview
    When the update tries to delete the previous snapshot
    Then the consumed snapshot remains
    And a repetition of the saving receives the saved letter

  Scenario: If a concurrent preview replaces the snapshot, nothing is saved
    Given Erika replaces the preview in another tab while she saves
    When the saving tries to consume the snapshot
    Then the snapshot is unknown
    And nothing is saved or stored

  Scenario: Concurrent saving of the same preview yields one letter
    Given a second request for the same preview consumes the snapshot while this one creates its PDF
    When Erika saves the preview
    Then she receives the letter of the second request
    And her own PDF is no longer stored

  Scenario: A new preview yields a new letter
    Given Erika has already saved the preview
    And Erika creates a new preview of the "Payment reminder" for customer 4711
    When Erika saves the new preview
    Then a second letter is saved

  Scenario Outline: If the letter is deleted, a repeated save finds nothing
    Given Erika has already saved the preview
    And the letter has since been deleted (<kind>)
    When Erika saves the preview again
    Then the snapshot is unknown
    And no further PDF was created

    Examples:
      | kind    |
      | hard    |
      | logical |

  Scenario Outline: If the letter of a concurrent request is deleted, nothing is saved
    Given a second request for the same preview consumes the snapshot while this one creates its PDF
    And the letter of the second request is then deleted (<kind>) before this one saves
    When Erika saves the preview
    Then the snapshot is unknown
    And her own PDF is no longer stored

    Examples:
      | kind    |
      | hard    |
      | logical |
