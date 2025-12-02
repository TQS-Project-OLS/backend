Feature: Admin Oversight
  As an admin
  I want to oversee bookings, payments, and user behavior
  So that I can ensure everything runs smoothly

  Background:
    Given the following instruments exist for admin oversight:
      | name          | type          | family    | age | price  | description        | ownerId |
      | Guitar Pro    | Electric      | String    | 2   | 50.0   | Electric guitar    | 10      |
      | Piano Premium | Digital       | Keyboard  | 1   | 100.0  | Digital piano      | 20      |
      | Violin Classic| Acoustic      | String    | 3   | 75.0   | Acoustic violin    | 10      |

  Scenario: Admin views all bookings in the system
    Given the following bookings exist:
      | itemName      | renterId | startDate  | endDate    | status   |
      | Guitar Pro    | 100      | 2025-12-01 | 2025-12-05 | PENDING  |
      | Piano Premium | 200      | 2025-12-10 | 2025-12-15 | APPROVED |
      | Violin Classic| 300      | 2025-12-20 | 2025-12-25 | REJECTED |
    When the admin requests all bookings
    Then the admin should see 3 bookings
    And the bookings should include all statuses

  Scenario: Admin filters bookings by status
    Given the following bookings exist:
      | itemName      | renterId | startDate  | endDate    | status   |
      | Guitar Pro    | 100      | 2025-12-01 | 2025-12-05 | PENDING  |
      | Piano Premium | 200      | 2025-12-10 | 2025-12-15 | PENDING  |
      | Violin Classic| 300      | 2025-12-20 | 2025-12-25 | APPROVED |
    When the admin filters bookings by status "PENDING"
    Then the admin should see 2 bookings
    And all bookings should have status "PENDING"

  Scenario: Admin views bookings by specific renter
    Given the following bookings exist:
      | itemName      | renterId | startDate  | endDate    | status   |
      | Guitar Pro    | 100      | 2025-12-01 | 2025-12-05 | PENDING  |
      | Piano Premium | 100      | 2025-12-10 | 2025-12-15 | APPROVED |
      | Violin Classic| 200      | 2025-12-20 | 2025-12-25 | APPROVED |
    When the admin views bookings for renter 100
    Then the admin should see 2 bookings
    And all bookings should be for renter 100

  Scenario: Admin cancels a booking
    Given the following bookings exist:
      | itemName      | renterId | startDate  | endDate    | status   |
      | Guitar Pro    | 100      | 2025-12-01 | 2025-12-05 | PENDING  |
    When the admin cancels the booking for "Guitar Pro"
    Then the booking status should be "CANCELLED"

  Scenario: Admin views booking statistics
    Given the following bookings exist:
      | itemName      | renterId | startDate  | endDate    | status    |
      | Guitar Pro    | 100      | 2025-12-01 | 2025-12-05 | PENDING   |
      | Piano Premium | 200      | 2025-12-10 | 2025-12-15 | APPROVED  |
      | Violin Classic| 300      | 2025-12-20 | 2025-12-25 | APPROVED  |
      | Guitar Pro    | 400      | 2025-12-26 | 2025-12-30 | REJECTED  |
      | Piano Premium | 500      | 2026-01-01 | 2026-01-05 | CANCELLED |
    When the admin requests booking statistics
    Then the statistics should show:
      | metric    | value |
      | total     | 5     |
      | pending   | 1     |
      | approved  | 2     |
      | rejected  | 1     |
      | cancelled | 1     |

  Scenario: Admin monitors renter activity
    Given the following bookings exist:
      | itemName      | renterId | startDate  | endDate    | status   |
      | Guitar Pro    | 100      | 2025-12-01 | 2025-12-05 | PENDING  |
      | Piano Premium | 100      | 2025-12-10 | 2025-12-15 | APPROVED |
      | Violin Classic| 100      | 2025-12-20 | 2025-12-25 | REJECTED |
    When the admin checks activity for renter 100
    Then the renter should have 3 bookings

  Scenario: Admin monitors owner activity
    Given the following bookings exist:
      | itemName      | renterId | startDate  | endDate    | status   |
      | Guitar Pro    | 100      | 2025-12-01 | 2025-12-05 | PENDING  |
      | Violin Classic| 200      | 2025-12-10 | 2025-12-15 | APPROVED |
      | Piano Premium | 300      | 2025-12-20 | 2025-12-25 | APPROVED |
    When the admin checks activity for owner 10
    Then the owner should have 2 bookings

  Scenario: Admin views revenue by owner
    Given the following bookings exist:
      | itemName      | renterId | startDate  | endDate    | status   |
      | Guitar Pro    | 100      | 2025-12-01 | 2025-12-05 | APPROVED |
      | Violin Classic| 200      | 2025-12-10 | 2025-12-15 | APPROVED |
    When the admin checks revenue for owner 10
    Then the owner revenue should be 575.0

  Scenario: Admin views total system revenue
    Given the following bookings exist:
      | itemName      | renterId | startDate  | endDate    | status   |
      | Guitar Pro    | 100      | 2025-12-01 | 2025-12-05 | APPROVED |
      | Piano Premium | 200      | 2025-12-10 | 2025-12-15 | APPROVED |
      | Violin Classic| 300      | 2025-12-20 | 2025-12-25 | APPROVED |
    When the admin requests total revenue
    Then the total system revenue should be 1075.0

  Scenario: Revenue calculation excludes non-approved bookings
    Given the following bookings exist:
      | itemName      | renterId | startDate  | endDate    | status    |
      | Guitar Pro    | 100      | 2025-12-01 | 2025-12-05 | PENDING   |
      | Piano Premium | 200      | 2025-12-10 | 2025-12-15 | REJECTED  |
      | Violin Classic| 300      | 2025-12-20 | 2025-12-25 | CANCELLED |
    When the admin requests total revenue
    Then the total system revenue should be 0.0