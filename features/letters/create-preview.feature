# Status: proposed (AI-assisted, awaiting review). Bridge to the tests:
# CreatePreviewServiceTest in the module usecases-service, one test per scenario,
# named after the scenario title. There the ports are replaced by fakes.
# Location as proposed in #46 (features/<area>/).
Feature: Create preview
  A clerk chooses a letter template for a business reference and sees a preview
  before saving the letter. The library first checks by itself whether the letter
  template fits the reference (ADR-020) and then asks the host for permission.
  Only with permission does it check whether there are published versions, and
  only then does the host deliver the data context. Each preview records its state
  in a snapshot that can be used for 30 minutes (ADR-007).

  Background:
    Given the letter template "Payment reminder" belongs to the reference type "customer"
    And its published version is 2, version 1 is superseded, and it has a draft 3
    And its letterhead has the published version 1
    And the host delivers for customer 4711 the data context "name" = "Erika Mustermann GmbH"

  Scenario: With permission, a preview with snapshot is created
    Given Erika may create a letter from the "Payment reminder" for customer 4711
    When Erika creates the preview of the "Payment reminder" for customer 4711
    Then the preview shows "Erika Mustermann GmbH"
    And a snapshot is saved, bound to Erika, customer 4711, version 1 of the letterhead and version 2 of the "Payment reminder"
    And the snapshot expires in 30 minutes

  Scenario: Without permission, no data context is requested
    Given Erika may not create a letter from the "Payment reminder" for customer 4711
    When Erika creates the preview of the "Payment reminder" for customer 4711
    Then the preview is not allowed
    And the host was not asked for the data context
    And nothing was rendered
    And no snapshot is saved

  Scenario: For a reference of another type, the host is not asked
    When Erika creates the preview of the "Payment reminder" for supplier 0815
    Then the letter template does not fit the reference
    And the host was asked neither for permission nor for the data context
    And no snapshot is saved

  Scenario: For an unknown letter template, there is no preview
    When Erika creates the preview of an unknown letter template for customer 4711
    Then the letter template is unknown
    And the host was asked neither for permission nor for the data context

  Scenario Outline: Without published version, there is no preview
    Given <template> has no published version
    And Erika may create a letter from the "Payment reminder" for customer 4711
    When Erika creates the preview of the "Payment reminder" for customer 4711
    Then the preview has no published version
    And the host was asked for permission but not for the data context
    And no snapshot is saved

    Examples:
      | template            |
      | the letter template |
      | the letterhead      |

  Scenario Outline: Without permission, it stays hidden whether a version is published
    Given <template> has no published version
    And Erika may not create a letter from the "Payment reminder" for customer 4711
    When Erika creates the preview of the "Payment reminder" for customer 4711
    Then the preview is not allowed
    And the host was not asked for the data context
    And nothing was rendered
    And no snapshot is saved

    Examples:
      | template            |
      | the letter template |
      | the letterhead      |

  Scenario: An updated preview replaces the own snapshot
    Given Erika may create a letter from the "Payment reminder" for customer 4711
    And Erika created a preview of the "Payment reminder" for customer 4711 20 minutes ago
    When Erika updates this preview
    Then the previous snapshot is deleted
    And the new snapshot expires in 30 minutes

  Scenario: An updated preview leaves a consumed snapshot in place
    Given Erika may create a letter from the "Payment reminder" for customer 4711
    And Erika has already saved a letter from her previous preview
    When Erika updates this preview
    Then the previous, consumed snapshot remains
    And there is the new snapshot in addition

  Scenario: A foreign snapshot behaves like a missing one
    Given Erika may create a letter from the "Payment reminder" for customer 4711
    And Max has created a preview of the "Payment reminder" for customer 4711
    When Erika updates a preview and specifies the snapshot of Max
    Then a new preview is created for Erika
    And the snapshot of Max is still saved unchanged
