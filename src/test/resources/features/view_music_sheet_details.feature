Feature: View details and photos of music sheets
  As a sheet renter
  I want to view details and photos of music sheets
  So that I know exactly what I'm renting

  Scenario: View detailed information of a music sheet with photos
    Given the following music sheet exists in the catalog:
      | name           | Moonlight Sonata                                    |
      | description    | Complete score with fingering annotations           |
      | price          | 15.99                                               |
      | category       | CLASSICAL                                           |
      | composer       | Ludwig van Beethoven                                |
      | instrumentation| Piano Solo                                          |
      | duration       | 15.5                                                |
      | photos         | /photos/moonlight1.jpg,/photos/moonlight2.jpg       |
    When I view the details of music sheet "Moonlight Sonata"
    Then I should see the sheet name "Moonlight Sonata"
    And I should see the sheet description "Complete score with fingering annotations"
    And I should see the sheet price "15.99"
    And I should see the category "CLASSICAL"
    And I should see the composer "Ludwig van Beethoven"
    And I should see the instrumentation "Piano Solo"
    And I should see the duration "15.5"
    And I should see 2 sheet photos

  Scenario: View detailed information of a music sheet without photos
    Given the following music sheet exists in the catalog:
      | name           | Autumn Leaves                   |
      | description    | Lead sheet with chord symbols   |
      | price          | 8.99                            |
      | category       | JAZZ                            |
      | composer       | Joseph Kosma                    |
      | instrumentation| Any Instrument                  |
      | duration       | 4.2                             |
    When I view the details of music sheet "Autumn Leaves"
    Then I should see the sheet name "Autumn Leaves"
    And I should see the sheet description "Lead sheet with chord symbols"
    And I should see the sheet price "8.99"
    And I should see the category "JAZZ"
    And I should see the composer "Joseph Kosma"
    And I should see 0 sheet photos

  Scenario: View detailed information of a music sheet with a single photo
    Given the following music sheet exists in the catalog:
      | name           | Bohemian Rhapsody                      |
      | description    | Full band arrangement                  |
      | price          | 25.99                                  |
      | category       | ROCK                                   |
      | composer       | Freddie Mercury                        |
      | instrumentation| Full Band                              |
      | duration       | 5.9                                    |
      | photos         | /photos/bohemian.jpg                   |
    When I view the details of music sheet "Bohemian Rhapsody"
    Then I should see the sheet name "Bohemian Rhapsody"
    And I should see the sheet description "Full band arrangement"
    And I should see 1 sheet photo

  Scenario: View detailed information of multiple music sheets
    Given the following music sheets exist in the catalog:
      | name              | description                    | price | category  | composer          | instrumentation | duration | photos                          |
      | Clair de Lune     | Romantic piano piece           | 12.99 | CLASSICAL | Claude Debussy    | Piano Solo      | 5.0      | /photos/clair.jpg               |
      | Take Five         | Jazz standard with odd meter   | 9.99  | JAZZ      | Paul Desmond      | Saxophone       | 5.4      | /photos/take5-1.jpg,/photos/take5-2.jpg |
    When I view the details of music sheet "Clair de Lune"
    Then I should see the sheet name "Clair de Lune"
    And I should see the sheet description "Romantic piano piece"
    And I should see 1 sheet photo
    When I view the details of music sheet "Take Five"
    Then I should see the sheet name "Take Five"
    And I should see the sheet description "Jazz standard with odd meter"
    And I should see 2 sheet photos

  Scenario: View music sheet details showing complete information
    Given the following music sheet exists in the catalog:
      | name           | The Four Seasons - Spring                                  |
      | description    | First movement from Vivaldi's famous violin concerto, includes solo violin part and piano reduction |
      | price          | 18.99                                                      |
      | category       | CLASSICAL                                                  |
      | composer       | Antonio Vivaldi                                            |
      | instrumentation| Violin and Piano                                           |
      | duration       | 10.2                                                       |
      | photos         | /photos/vivaldi1.jpg,/photos/vivaldi2.jpg,/photos/vivaldi3.jpg |
    When I view the details of music sheet "The Four Seasons - Spring"
    Then I should see the sheet name "The Four Seasons - Spring"
    And I should see the sheet description containing "Vivaldi's famous violin concerto"
    And I should see the sheet description containing "solo violin part"
    And I should see 3 sheet photos

  Scenario: Attempt to view details of non-existent music sheet
    Given no music sheets exist in the catalog
    When I attempt to view the details of music sheet with ID 999
    Then I should receive a sheet error message "Music sheet not found"
