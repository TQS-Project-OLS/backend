Feature: Secure Payment for Instrument Rental
  As any renter
  I want to pay securely for an instrument rental
  So that I can complete the transaction safely

  Background:
    Given the payment system is available
    And I am an authenticated renter

  Scenario: Successful payment with valid credit card
    Given I have an approved booking for instrument with ID 1
    When I initiate payment with method "CREDIT_CARD"
    And I provide valid card number "4242424242424242"
    Then the payment should be processed successfully
    And the payment status should be "COMPLETED"
    And I should receive a transaction ID

  Scenario: Failed payment with declined card
    Given I have an approved booking for instrument with ID 1
    When I initiate payment with method "CREDIT_CARD"
    And I provide declined card number "4000000000000002"
    Then the payment should fail
    And the payment status should be "FAILED"

  Scenario: Cannot pay for pending booking
    Given I have a pending booking for instrument with ID 1
    When I try to initiate payment with method "CREDIT_CARD"
    Then I should receive an error message about booking not being approved

  Scenario: Check payment status for completed payment
    Given I have a completed payment for booking ID 1
    When I check the payment status for booking ID 1
    Then the system should confirm the booking is paid

  Scenario: Refund a completed payment
    Given I have a completed payment with ID 1
    When I request a refund for payment ID 1
    Then the payment status should be "REFUNDED"
