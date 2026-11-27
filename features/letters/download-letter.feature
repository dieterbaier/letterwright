# Status: proposed (AI-assisted, awaiting review). Bridge to the tests:
# DownloadLetterServiceTest in the module usecases-service, one test per scenario,
# named after the scenario title. There the ports are replaced by fakes.
Feature: Download letter
  A clerk downloads the PDF of a saved letter. They get exactly the PDF that was
  checked when saving (ADR-007, QS-008). A letter they may not download does not
  exist for them (QS-009).

  Background:
    Given a letter is saved for customer 4711
    And Erika may download the letters for customer 4711

  Scenario: The download delivers the unchanged PDF
    When Erika downloads the letter
    Then she receives the PDF with the checksum from saving

  Scenario: A letter that is not allowed behaves like a missing one
    Given Max may not download the letters for customer 4711
    When Max downloads the letter
    Then the letter is unknown
    And no PDF was read

  Scenario: An unknown letter does not exist
    When Erika downloads a letter with an unknown ID
    Then the letter is unknown
    And the host was not asked

  Scenario: A logically deleted letter does not exist
    Given the letter is logically deleted
    When Erika downloads the letter
    Then the letter is unknown
    And the host was not asked
    And no PDF was read

  Scenario: A changed PDF is not delivered
    Given the document store delivers other bytes than when saving
    When Erika downloads the letter
    Then the download fails with an error
