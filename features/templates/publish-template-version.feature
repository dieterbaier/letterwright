# Status: proposed (AI-assisted, awaiting review). Bridge to the tests:
# PublishTemplateVersionServiceTest in the module usecases-service, one test per
# scenario, named after the scenario title. There the ports are replaced by fakes.
Feature: Publish template version
  A template manager publishes the draft of a letterhead or letter template. Per
  template there is at most one published version; new letters are created with
  it, saved letters keep their versions (ADR-007, ADR-022). Beforehand, a proof
  PDF is created with the sample data of the version and checked like every saved
  letter (ADR-005). If it does not pass the check, the draft stays a draft.

  Background:
    Given the letterhead "Standard" has the published version 1 with the sample data "sender" = "Muster GmbH" and "subject" = "General"
    And the letter template "Payment reminder" uses this letterhead
    And its version 1 is published, and its draft 2 has the sample data "name" = "Erika Mustermann GmbH" and "subject" = "Payment reminder"
    And Vera may publish templates

  Scenario: A conformant draft is published and supersedes the previous version
    When Vera publishes the payment reminder
    Then version 2 of the payment reminder is published
    And version 1 is superseded and unchanged

  Scenario: The proof PDF is created with the published letterhead and the sample data of both versions
    When Vera publishes the payment reminder
    Then the proof PDF is created from letterhead version 1 and payment reminder version 2
    And with the sample data "sender" = "Muster GmbH", "subject" = "Payment reminder" and "name" = "Erika Mustermann GmbH"
    And exactly this proof PDF was checked

  Scenario: A letterhead is checked with the built-in sample content and published
    Given the letterhead has a draft 2
    When Vera publishes the letterhead
    Then the proof PDF is created from the draft and the built-in sample content
    And with the sample data of the letterhead
    And version 2 of the letterhead is published, version 1 superseded

  Scenario: If the proof PDF fails the check, the version stays a draft
    Given the check finds "Image without alternative text"
    When Vera publishes the payment reminder
    Then the payment reminder is not conformant with the finding "Image without alternative text"
    And the payment reminder is unchanged

  Scenario: If the check cannot run, nothing is published
    Given the check cannot be run
    When Vera publishes the payment reminder
    Then the publishing fails with a technical error
    And the payment reminder is unchanged

  Scenario: Without permission, nothing is checked and nothing published
    When Max publishes the payment reminder
    Then the publishing is not allowed
    And no proof PDF was created
    And the payment reminder is unchanged

  Scenario: Without draft, there is nothing to publish
    Given the payment reminder has no draft
    When Vera publishes the payment reminder
    Then the payment reminder has no draft

  Scenario: A draft without content is not published
    Given the draft of the payment reminder contains only whitespace
    When Vera publishes the payment reminder
    Then the draft has no content
    And no proof PDF was created

  Scenario: Without published letterhead, the letter template is not published
    Given the letterhead is withdrawn
    When Vera publishes the payment reminder
    Then the letterhead is not published
    And no proof PDF was created

  Scenario: An unknown template is reported
    When Vera publishes a template with an unknown ID
    Then the template is unknown

  Scenario: If someone else changed the template meanwhile, nothing is published
    Given another template manager changes the draft while Vera publishes
    When Vera publishes the payment reminder
    Then the state is stale
    And the changed, unchecked draft stays a draft

  Scenario Outline: If the letterhead changes during the check, the check time counts
    Given another template manager has <change> the letterhead while the payment reminder is being checked
    When Vera publishes the payment reminder
    Then the proof PDF was checked with letterhead version 1
    And version 2 of the payment reminder is published
    And the letterhead is <afterwards>

    Examples:
      | change                    | afterwards                   |
      | superseded by version 2   | published in version 2       |
      | withdrawn                 | without a published version  |

  Scenario: A published version stays unchanged, a change starts a new draft
    Given Vera has published the payment reminder
    When the published version is to be changed
    Then this is rejected
    And a new draft 3 takes over the sample data of version 2
