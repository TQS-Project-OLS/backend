Feature: Filter instruments by type
  As an instrument renter
  I want to filter instruments by type
  So that I can find instruments of a specific type

  Scenario: Filter instruments by ELECTRIC type with multiple matches
    Given the following instruments exist for type filter:
      | name                 | type     | family    | age | price   | description           |
      | Fender Stratocaster  | ELECTRIC | Guitar    | 5   | 899.99  | Classic rock guitar   |
      | Gibson Les Paul      | ELECTRIC | Guitar    | 3   | 1299.99 | Premium electric      |
      | Yamaha P-125         | DIGITAL  | Keyboard  | 2   | 599.99  | Excellent condition   |
    When I filter instruments by type "ELECTRIC"
    Then the filter should return 2 instruments
    And all filtered instruments should have type "ELECTRIC"

  Scenario: Filter instruments by type with single match
    Given the following instruments exist for type filter:
      | name                 | type     | family    | age | price   | description           |
      | Fender Stratocaster  | ELECTRIC | Guitar    | 5   | 899.99  | Classic rock guitar   |
      | Yamaha P-125         | DIGITAL  | Keyboard  | 2   | 599.99  | Excellent condition   |
    When I filter instruments by type "DIGITAL"
    Then the filter should return 1 instrument
    And the first filtered instrument should have name "Yamaha P-125"

  Scenario: Filter instruments by type with no matches
    Given the following instruments exist for type filter:
      | name                 | type     | family    | age | price   | description           |
      | Fender Stratocaster  | ELECTRIC | Guitar    | 5   | 899.99  | Classic rock guitar   |
      | Yamaha P-125         | DIGITAL  | Keyboard  | 2   | 599.99  | Excellent condition   |
    When I filter instruments by type "SYNTHESIZER"
    Then the filter should return 0 instruments

  Scenario: Filter instruments by ACOUSTIC type
    Given the following instruments exist for type filter:
      | name                 | type     | family    | age | price   | description           |
      | Martin D-28          | ACOUSTIC | Guitar    | 10  | 2899.99 | Premium acoustic      |
      | Taylor 214ce         | ACOUSTIC | Guitar    | 2   | 1099.99 | Great sound quality   |
      | Yamaha P-125         | DIGITAL  | Keyboard  | 2   | 599.99  | Excellent condition   |
    When I filter instruments by type "ACOUSTIC"
    Then the filter should return 2 instruments
    And all filtered instruments should have type "ACOUSTIC"
