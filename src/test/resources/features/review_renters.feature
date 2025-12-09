Feature: Rate renters after they return items
  As an owner
  I want to rate renters after they return my items
  So that I build trust with future customers

  Background:
    Given there is an owner "alice_owner" with ID 1
    And there is a renter "bob_renter" with ID 2
    And the owner has an instrument "Gibson Les Paul" with price 1499.99
    And the renter has a completed booking for the instrument that ended 3 days ago

  Scenario: Owner successfully reviews a renter after rental ends
    When the owner submits a renter review with score 5 and comment "Excellent renter! Took great care of the instrument."
    Then the renter review should be successfully created
    And the renter review should have score 5
    And the renter review should have comment "Excellent renter! Took great care of the instrument."
    And the average score for the renter should be 5.0

  Scenario: Owner cannot review renter with invalid score (too high)
    When the owner submits a renter review with score 6 and comment "Great"
    Then the renter review should fail with error "Score must be between 1 and 5"

  Scenario: Owner cannot review renter with invalid score (too low)
    When the owner submits a renter review with score 0 and comment "Bad"
    Then the renter review should fail with error "Score must be between 1 and 5"

  Scenario: Only item owner can review the renter
    Given there is an owner "other_owner" with ID 3
    When user "other_owner" tries to review the renter for the booking
    Then the renter review should fail with error "You can only review renters for your own items"

  Scenario: Owner cannot review renter before booking ends
    Given the renter has an active rental that ends in 2 days
    When the owner tries to review the renter for the active rental
    Then the renter review should fail with error "Cannot review a booking that hasn't ended yet"

  Scenario: Owner cannot review the same renter twice for the same booking
    Given the owner has already reviewed the renter for the booking with score 4
    When the owner submits another renter review with score 5 and comment "Changed my mind"
    Then the renter review should fail with error "A review already exists for this booking"

  Scenario: Multiple owners review the same renter
    Given there is an owner "charlie_owner" with ID 4
    And user "charlie_owner" has an instrument "Violin"
    And the renter has a completed booking for "charlie_owner" instrument that ended 1 day ago
    And the first owner has reviewed the renter with score 5
    When user "charlie_owner" submits a renter review with score 3 and comment "Okay renter"
    Then both renter reviews should exist
    And the average score for the renter should be 4.0

  Scenario: Get all reviews for a renter
    Given there are 3 reviews for the renter with scores 5, 4, and 3
    When I request all reviews for the renter
    Then I should receive 3 renter reviews
    And the renter reviews should be ordered by creation date

  Scenario: Check if owner can review a renter for a booking
    When the owner checks if they can review the renter for the completed booking
    Then the response should indicate they can review

  Scenario: Check if owner can review a renter for active booking
    Given the renter has an active rental that ends in 2 days
    When the owner checks if they can review the renter for the active rental
    Then the response should indicate they cannot review

  Scenario: Owner and renter both review after booking ends
    Given the renter has already reviewed the booking with score 5
    When the owner submits a renter review with score 4 and comment "Good renter"
    Then both the item review and renter review should exist
    And the booking should have reviews from both parties
