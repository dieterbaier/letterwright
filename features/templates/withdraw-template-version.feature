# Status: proposed (AI-assisted, awaiting review). Bridge to the tests:
# WithdrawTemplateVersionServiceTest in the module usecases-service, one test per
# scenario, named after the scenario title. There the ports are replaced by fakes.
Feature: Withdraw template version
  A template manager withdraws the published version of a letterhead or letter
  template without a successor. Until the next publication, no preview and no
  letter is created with the template; saved letters stay unchanged (ADR-007,
  ADR-022). Withdrawing requires the same permission as publishing.

  Background:
    Given the template has the published version 1 and a draft 2
    And Vera may publish templates

  Scenario Outline: The published version is withdrawn without successor
    Given the template is <template>
    When Vera withdraws the template
    Then version 1 is withdrawn
    And the template has no published version
    And the draft 2 is unchanged

    Examples:
      | template          |
      | a letter template |
      | a letterhead      |

  Scenario: Without permission, nothing is withdrawn
    When Max withdraws the template
    Then the withdrawing is not allowed
    And the template is unchanged

  Scenario Outline: Without published version, there is nothing to withdraw
    Given the template is <template> with only a draft
    When Vera withdraws the template
    Then nothing is published

    Examples:
      | template          |
      | a letter template |
      | a letterhead      |

  Scenario: An unknown template is reported
    When Vera withdraws a template with an unknown ID
    Then the template is unknown

  Scenario: If someone else changed the template meanwhile, nothing is withdrawn
    Given another template manager publishes the draft while Vera withdraws
    When Vera withdraws the template
    Then the state is stale
    And version 2 stays published
