Feature: Register music sheets with details and photos
  As a sheet owner
  I want to register my music sheets with details and photos
  So that renters can find them

  @register_music_sheet
  Scenario: Register a music sheet with complete details including photos
    Given I am a sheet owner with ID 5
    When I register a music sheet with the following details:
      | name            | Für Elise                                      |
      | description     | Famous Beethoven composition for piano         |
      | price           | 7.99                                           |
      | category        | CLASSICAL                                      |
      | composer        | Ludwig van Beethoven                           |
      | instrumentation | Piano                                          |
      | duration        | 3.5                                            |
      | photos          | /photos/sheet1.jpg,/photos/sheet2.jpg          |
    Then the music sheet should be successfully registered
    And the music sheet should have 2 photos attached
    And I should be able to search for the sheet by name "Für Elise"

  @register_music_sheet
  Scenario: Register a music sheet without photos
    Given I am a sheet owner with ID 7
    When I register a music sheet with the following details:
      | name            | Claire de Lune                |
      | description     | Debussy piano piece           |
      | price           | 8.99                          |
      | category        | IMPRESSIONIST                 |
      | composer        | Claude Debussy                |
      | instrumentation | Piano                         |
      | duration        | 4.5                           |
    Then the music sheet should be successfully registered
    And the music sheet should have 0 photos attached

  @register_music_sheet
  Scenario: Register a music sheet with a single photo
    Given I am a sheet owner with ID 4
    When I register a music sheet with the following details:
      | name            | Canon in D                    |
      | description     | Pachelbel's Canon             |
      | price           | 6.99                          |
      | category        | BAROQUE                       |
      | composer        | Johann Pachelbel              |
      | instrumentation | String Quartet                |
      | duration        | 5.0                           |
      | photos          | /photos/canon.jpg             |
    Then the music sheet should be successfully registered
    And the music sheet should have 1 photo attached
    And the registered music sheet should have name "Canon in D"
    And the registered music sheet should have description "Pachelbel's Canon"
    And the registered music sheet should have price 6.99

  @register_music_sheet
  Scenario: Register multiple music sheets from the same owner
    Given I am a sheet owner with ID 3
    When I register a music sheet with the following details:
      | name            | Moonlight Sonata              |
      | description     | Piano Sonata No. 14           |
      | price           | 9.99                          |
      | category        | CLASSICAL                     |
      | composer        | Beethoven                     |
      | instrumentation | Piano                         |
      | photos          | /photos/moon1.jpg,/photos/moon2.jpg,/photos/moon3.jpg |
    And I register another music sheet with the following details:
      | name            | Autumn Leaves                 |
      | description     | Jazz standard                 |
      | price           | 7.99                          |
      | category        | JAZZ                          |
      | composer        | Joseph Kosma                  |
      | instrumentation | Piano                         |
      | photos          | /photos/autumn.jpg            |
    Then both music sheets should be successfully registered
    And I should be able to search for "Moonlight" and find 1 music sheet
    And I should be able to search for "Autumn" and find 1 music sheet

  @register_music_sheet
  Scenario: Register a music sheet and verify all details are saved
    Given I am a sheet owner with ID 2
    When I register a music sheet with the following details:
      | name            | The Four Seasons              |
      | description     | Vivaldi's famous concertos    |
      | price           | 15.99                         |
      | category        | BAROQUE                       |
      | composer        | Antonio Vivaldi               |
      | instrumentation | Violin and Orchestra          |
      | duration        | 40.0                          |
      | photos          | /photos/vivaldi.jpg           |
    Then the music sheet should be successfully registered
    And the registered music sheet should have all the details I provided

  @register_music_sheet
  Scenario: Register a music sheet and verify it can be filtered by category
    Given I am a sheet owner with ID 6
    When I register a music sheet with the following details:
      | name            | Blue Rondo à la Turk          |
      | description     | Dave Brubeck jazz piece       |
      | price           | 11.99                         |
      | category        | JAZZ                          |
      | composer        | Dave Brubeck                  |
      | instrumentation | Jazz Ensemble                 |
      | duration        | 6.5                           |
    Then the music sheet should be successfully registered
    And I should be able to filter by category "JAZZ" and find the sheet "Blue Rondo à la Turk"
