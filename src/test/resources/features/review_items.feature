Feature: Rate instruments and sheets after renting
  As a renter
  I want to rate an instrument or sheet after renting it
  So that I help others make informed decisions

  Background:
    Given there is a user "john_owner" with ID 1
    And there is a user "jane_renter" with ID 2
    And the owner has an instrument "Yamaha P-125" with price 599.99
    And the renter has a completed booking for the instrument that ended 2 days ago

  Scenario: Renter successfully reviews a completed rental
    When the renter submits a review with score 5 and comment "Excellent piano! Great sound quality."
    Then the review should be successfully created
    And the review should have score 5
    And the review should have comment "Excellent piano! Great sound quality."
    And the average score for the instrument should be 5.0

  Scenario: Renter cannot review with invalid score (too high)
    When the renter submits a review with score 6 and comment "Great"
    Then the review should fail with error "Score must be between 1 and 5"

  Scenario: Renter cannot review with invalid score (too low)
    When the renter submits a review with score 0 and comment "Bad"
    Then the review should fail with error "Score must be between 1 and 5"

  Scenario: Renter cannot review a booking from another user
    Given there is a user "other_renter" with ID 3
    When user "other_renter" tries to review the booking
    Then the review should fail with error "You can only review your own bookings"

  Scenario: Renter cannot review before booking ends
    Given the renter has an active booking that ends in 2 days
    When the renter tries to review the active booking
    Then the review should fail with error "Cannot review a booking that hasn't ended yet"

  Scenario: Renter cannot review the same booking twice
    Given the renter has already reviewed the booking with score 4
    When the renter submits another review with score 5 and comment "Changed my mind"
    Then the review should fail with error "A review already exists for this booking"

  Scenario: Multiple renters review the same instrument
    Given there is a user "bob_renter" with ID 4
    And user "bob_renter" has a completed booking for the instrument that ended 1 day ago
    And the first renter has reviewed the booking with score 5
    When user "bob_renter" submits a review with score 3 and comment "Decent"
    Then both reviews should exist
    And the average score for the instrument should be 4.0

  Scenario: Get all reviews for an instrument
    Given there are 3 reviews for the instrument with scores 5, 4, and 3
    When I request all reviews for the instrument
    Then I should receive 3 reviews
    And the reviews should be ordered by creation date

  Scenario: Check if booking can be reviewed
    When I check if the completed booking can be reviewed
    Then the response should indicate it can be reviewed

  Scenario: Check if active booking can be reviewed
    Given the renter has an active booking that ends in 2 days
    When I check if the active booking can be reviewed
    Then the response should indicate it cannot be reviewed
