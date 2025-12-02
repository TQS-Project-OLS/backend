Feature: Manage Instrument Availability
  As an instrument owner
  I want to manage availability periods for my instruments
  So that renters know when instruments cannot be booked

  Background:
    Given the following instruments exist for availability:
      | id | name            | type     | family   | age | price | ownerId |
      | 1  | Gibson Les Paul | ELECTRIC | STRING   | 3   | 75.0  | 1       |
      | 2  | Roland Piano    | DIGITAL  | KEYBOARD | 2   | 60.0  | 1       |

  Scenario: Owner blocks dates for personal use
    When I create an unavailability period for instrument 1 from "2025-12-10" to "2025-12-15" with reason "OWNER_USE"
    Then the unavailability should be created successfully
    And the instrument 1 should not be available from "2025-12-10" to "2025-12-15"

  Scenario: Owner blocks dates for maintenance
    When I create an unavailability period for instrument 1 from "2025-12-20" to "2025-12-25" with reason "MAINTENANCE"
    Then the unavailability should be created successfully
    And the instrument 1 should not be available from "2025-12-20" to "2025-12-25"

  Scenario: Owner blocks dates for other reasons
    When I create an unavailability period for instrument 1 from "2026-01-01" to "2026-01-05" with reason "OTHER"
    Then the unavailability should be created successfully

  Scenario: Multiple unavailability periods for same instrument
    When I create an unavailability period for instrument 1 from "2025-12-10" to "2025-12-15" with reason "OWNER_USE"
    And I create an unavailability period for instrument 1 from "2025-12-20" to "2025-12-25" with reason "MAINTENANCE"
    And I create an unavailability period for instrument 1 from "2026-01-10" to "2026-01-15" with reason "OTHER"
    Then the instrument 1 should have 3 unavailability periods

  Scenario: Check instrument is not available when dates overlap with block
    Given an unavailability period exists for instrument 1 from "2025-12-10" to "2025-12-15" with reason "OWNER_USE"
    When I check availability for instrument 1 from "2025-12-12" to "2025-12-17"
    Then the instrument should not be available

  Scenario: Check instrument is available when dates don't overlap
    Given an unavailability period exists for instrument 1 from "2025-12-10" to "2025-12-15" with reason "OWNER_USE"
    When I check availability for instrument 1 from "2025-12-20" to "2025-12-25"
    Then the instrument should be available

  Scenario: Check instrument is available when no blocks exist
    When I check availability for instrument 1 from "2025-12-10" to "2025-12-15"
    Then the instrument should be available

  Scenario: Owner retrieves all unavailability periods for an instrument
    Given an unavailability period exists for instrument 1 from "2025-12-10" to "2025-12-15" with reason "OWNER_USE"
    And an unavailability period exists for instrument 1 from "2025-12-20" to "2025-12-25" with reason "MAINTENANCE"
    And an unavailability period exists for instrument 2 from "2025-12-12" to "2025-12-18" with reason "OTHER"
    When I retrieve all unavailability periods for instrument 1
    Then I should receive 2 unavailability periods
    And the periods should include dates from "2025-12-10" to "2025-12-15"
    And the periods should include dates from "2025-12-20" to "2025-12-25"

  Scenario: Delete an unavailability period
    Given an unavailability period with id 1 exists for instrument 1 from "2025-12-10" to "2025-12-15" with reason "OWNER_USE"
    When I delete the unavailability period 1
    Then the unavailability period should be deleted successfully
    And the instrument 1 should be available from "2025-12-10" to "2025-12-15"

  Scenario: Cannot create unavailability with invalid dates
    When I try to create an unavailability period for instrument 1 from "2025-12-20" to "2025-12-10" with reason "OWNER_USE"
    Then the unavailability creation should fail
