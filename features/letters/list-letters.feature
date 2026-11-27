# Status: proposed (AI-assisted, awaiting review). Bridge to the tests:
# ListLettersServiceTest in the module usecases-service, one test per scenario,
# named after the scenario title. There the ports are replaced by fakes.
# Filtering, sorting and counting happen in the LetterRepository; the fake
# keeps its contract. The proof for the database is provided by the adapter (#6).
Feature: List letters
  A clerk sees the saved letters for a business reference, page by page and the
  newest first. Whether they may see them is decided by the host per reference.
  What they may not see counts neither in the page nor in the total (QS-009).

  Background:
    Given 3 letters are saved for customer 4711
    And Erika may view the letters for customer 4711

  Scenario: The list shows the letters for the reference page by page, newest first
    When Erika requests the first page with 2 entries for customer 4711
    Then she sees the 2 newest letters, the newest first
    And the total is 3 on 2 pages

  Scenario: Invisible letters count neither in the page nor in the total
    Given 2 letters are saved for customer 0815
    And the newest letter for customer 4711 is logically deleted
    When Erika requests the first page with 2 entries for customer 4711
    Then she sees the 2 remaining letters for customer 4711
    And the total is 2 on 1 page

  Scenario: A page beyond the end is empty and counts the visible letters
    When Erika requests the third page with 2 entries for customer 4711
    Then she sees no letters
    And the total is 3 on 2 pages

  Scenario: Without permission to view, nothing is read
    Given Erika may not view the letters for customer 4711
    When Erika requests the first page with 2 entries for customer 4711
    Then the list is not allowed
    And no letters were read or counted
