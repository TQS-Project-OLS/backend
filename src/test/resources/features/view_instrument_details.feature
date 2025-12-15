Feature: View detailed descriptions and photos of instruments
  As an instrument renter
  I want to view detailed descriptions and photos of instruments
  So that I can evaluate their condition and suitability

  Scenario: View detailed information of an instrument with photos
    Given the following instrument exists in the catalog:
      | name        | Fender Stratocaster                           |
      | description | Classic electric guitar in excellent condition with original pickups |
      | price       | 899.99                                        |
      | age         | 5                                             |
      | type        | ELECTRIC                                      |
      | family      | GUITAR                                        |
      | photos      | /photos/strat1.jpg,/photos/strat2.jpg,/photos/strat3.jpg |
    When I view the details of instrument "Fender Stratocaster"
    Then I should see the instrument name "Fender Stratocaster"
    And I should see the description "Classic electric guitar in excellent condition with original pickups"
    And I should see the price "899.99"
    And I should see the age "5"
    And I should see the type "ELECTRIC"
    And I should see the family "GUITAR"
    And I should see 3 photos

  Scenario: View detailed information of an instrument without photos
    Given the following instrument exists in the catalog:
      | name        | Roland TD-17                  |
      | description | Electronic drum kit with mesh heads |
      | price       | 1299.99                       |
      | age         | 1                             |
      | type        | DRUMS                         |
      | family      | PERCUSSION                    |
    When I view the details of instrument "Roland TD-17"
    Then I should see the instrument name "Roland TD-17"
    And I should see the description "Electronic drum kit with mesh heads"
    And I should see the price "1299.99"
    And I should see the age "1"
    And I should see the type "DRUMS"
    And I should see the family "PERCUSSION"
    And I should see 0 photos

  Scenario: View detailed information of an instrument with a single photo
    Given the following instrument exists in the catalog:
      | name        | Yamaha P-125                                  |
      | description | 88-key digital piano with weighted keys       |
      | price       | 599.99                                        |
      | age         | 2                                             |
      | type        | DIGITAL                                       |
      | family      | KEYBOARD                                      |
      | photos      | /photos/yamaha-p125.jpg                       |
    When I view the details of instrument "Yamaha P-125"
    Then I should see the instrument name "Yamaha P-125"
    And I should see the description "88-key digital piano with weighted keys"
    And I should see 1 photo

  Scenario: View detailed information of multiple instruments
    Given the following instruments exist in the catalog:
      | name                | description                           | price   | age | type     | family     | photos                        |
      | Taylor 814ce        | Premium acoustic guitar               | 3299.99 | 0   | ACOUSTIC | GUITAR     | /photos/taylor1.jpg           |
      | Yamaha YAS-280      | Alto saxophone for beginners          | 1299.99 | 1   | WIND     | WOODWIND   | /photos/sax1.jpg,/photos/sax2.jpg |
    When I view the details of instrument "Taylor 814ce"
    Then I should see the instrument name "Taylor 814ce"
    And I should see the description "Premium acoustic guitar"
    And I should see 1 photo
    When I view the details of instrument "Yamaha YAS-280"
    Then I should see the instrument name "Yamaha YAS-280"
    And I should see the description "Alto saxophone for beginners"
    And I should see 2 photos

  Scenario: View instrument details showing condition through description
    Given the following instrument exists in the catalog:
      | name        | Gibson Les Paul Standard              |
      | description | Vintage sunburst finish, minor cosmetic wear on body, all electronics working perfectly, professionally maintained |
      | price       | 2499.99                               |
      | age         | 10                                    |
      | type        | ELECTRIC                              |
      | family      | GUITAR                                |
      | photos      | /photos/gibson1.jpg,/photos/gibson2.jpg,/photos/gibson3.jpg,/photos/gibson4.jpg |
    When I view the details of instrument "Gibson Les Paul Standard"
    Then I should see the instrument name "Gibson Les Paul Standard"
    And I should see the description containing "professionally maintained"
    And I should see the description containing "minor cosmetic wear"
    And I should see 4 photos

  Scenario: Attempt to view details of non-existent instrument
    Given no instruments exist in the catalog
    When I attempt to view the details of instrument with ID 999
    Then I should receive an error message "Instrument not found"
