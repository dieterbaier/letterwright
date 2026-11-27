# Status: proposed (AI-assisted, awaiting review). Bridge to the tests: one test
# per scenario, named after the scenario title. The first four scenarios are
# checked by SaveLetterServiceTest in the module usecases-service against fakes of
# the ports, the last one by LetterwrightAutoConfigurationTest in the module
# spring-boot-starter. It checks only the wiring without a callback of the host;
# that an accepted letter is saved is shown by the first scenario. Saving end to
# end through the user interface comes only with the REST endpoint in #10.
# That the snapshot stays open after a rejection is achieved by the rollback at the
# outer boundary; the REST endpoint in #10 checks this.
Feature: Letter save callback
  When a clerk saves through the user interface of the library, the host
  application can change its own data in the same commit or reject the saving
  with a reason (ADR-023). For this it optionally provides the callback
  LetterSaveCallback. It runs after the database changes of the library and before
  the commit, and only if the current invocation creates the letter. There is no
  callback via the Java API.

  Background:
    Given Erika created a preview of the "Payment reminder" for customer 4711 10 minutes ago
    And Erika may create a letter from the "Payment reminder" for customer 4711
    And the host still delivers the same data context for customer 4711

  Scenario: If saving creates a letter, the host receives a callback
    When Erika saves the preview
    Then a letter is saved
    And the host received exactly one callback with this letter and Erika

  Scenario: A repetition triggers no further callback
    Given Erika has already saved the preview
    When Erika saves the preview again
    Then she receives the already saved letter
    And the host received no further callback

  Scenario: The host rejects the new letter with a reason
    Given the host rejects every new letter with the reason "The task is already done"
    When Erika saves the preview
    Then the saving is rejected by the host with the reason "The task is already done"

  Scenario: An error in the callback is a technical error
    Given the host's callback fails with an error
    When Erika saves the preview
    Then the saving fails with this error
    And the library did not repeat the callback

  Scenario: Without a callback of the host, the library accepts every new letter
    Given the host application provides no callback
    When the library consults the callback after creating a letter
    Then the new letter is accepted
