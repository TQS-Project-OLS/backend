Feature: Search for instruments by name
  As an instrument renter
  I want to search for instruments by name
  So that I can quickly find what I want to rent

  Scenario: Search instruments by exact name match
    Given the following instruments exist:
      | name                | type          | family    | age | price  | description           |
      | Yamaha P-125        | Digital Piano | Keyboard  | 2   | 599.99 | Excellent condition   |
      | Fender Stratocaster | Electric      | Guitar    | 5   | 899.99 | Classic rock guitar   |
      | Yamaha YAS-280      | Alto Sax      | Woodwind  | 1   | 1299.99| Professional quality |
    When I search for instruments with name "Yamaha P-125"
    Then I should receive 1 instrument
    And the first instrument should have name "Yamaha P-125"

  Scenario: Search instruments by partial name match
    Given the following instruments exist:
      | name                | type          | family    | age | price  | description           |
      | Yamaha P-125        | Digital Piano | Keyboard  | 2   | 599.99 | Excellent condition   |
      | Yamaha YAS-280      | Alto Sax      | Woodwind  | 1   | 1299.99| Professional quality |
      | Fender Stratocaster | Electric      | Guitar    | 5   | 899.99 | Classic rock guitar   |
    When I search for instruments with name "Yamaha"
    Then I should receive 2 instruments

  Scenario: Search instruments with no matches
    Given the following instruments exist:
      | name                | type          | family    | age | price  | description           |
      | Yamaha P-125        | Digital Piano | Keyboard  | 2   | 599.99 | Excellent condition   |
    When I search for instruments with name "Gibson"
    Then I should receive 0 instruments

  Scenario: Search is case insensitive
    Given the following instruments exist:
      | name                | type          | family    | age | price  | description           |
      | Yamaha P-125        | Digital Piano | Keyboard  | 2   | 599.99 | Excellent condition   |
    When I search for instruments with name "yamaha"
    Then I should receive 1 instrument
