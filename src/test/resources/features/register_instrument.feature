Feature: Register instruments with photos and descriptions
  As an instrument owner
  I want to register my instruments with photos and descriptions
  So that renters can find them

  Scenario: Register an instrument with complete details including photos
    Given I am an instrument owner with ID 5
    When I register an instrument with the following details:
      | name        | Gibson Les Paul                                   |
      | description | Classic electric guitar in excellent condition    |
      | price       | 1499.99                                           |
      | age         | 3                                                 |
      | type        | ELECTRIC                                          |
      | family      | GUITAR                                            |
      | photos      | /photos/guitar1.jpg,/photos/guitar2.jpg           |
    Then the instrument should be successfully registered
    And the instrument should have 2 photos attached
    And I should be able to search for it by name "Gibson Les Paul"

  Scenario: Register an instrument without photos
    Given I am an instrument owner with ID 7
    When I register an instrument with the following details:
      | name        | Roland TD-17                  |
      | description | Electronic drum kit           |
      | price       | 1299.99                       |
      | age         | 1                             |
      | type        | DRUMS                         |
      | family      | PERCUSSION                    |
    Then the instrument should be successfully registered
    And the instrument should have 0 photos attached

  Scenario: Register an instrument with a single photo
    Given I am an instrument owner with ID 4
    When I register an instrument with the following details:
      | name        | Taylor 814ce                  |
      | description | Premium acoustic guitar       |
      | price       | 3299.99                       |
      | age         | 0                             |
      | type        | ACOUSTIC                      |
      | family      | GUITAR                        |
      | photos      | /photos/taylor.jpg            |
    Then the instrument should be successfully registered
    And the instrument should have 1 photo attached
    And the registered instrument should have name "Taylor 814ce"
    And the registered instrument should have description "Premium acoustic guitar"
    And the registered instrument should have price 3299.99

  Scenario: Register multiple instruments from the same owner
    Given I am an instrument owner with ID 3
    When I register an instrument with the following details:
      | name        | Fender Jazz Bass              |
      | description | Professional bass guitar      |
      | price       | 999.99                        |
      | age         | 2                             |
      | type        | BASS                          |
      | family      | GUITAR                        |
      | photos      | /photos/bass1.jpg,/photos/bass2.jpg,/photos/bass3.jpg |
    And I register another instrument with the following details:
      | name        | Korg Minilogue                |
      | description | Analog synthesizer            |
      | price       | 649.99                        |
      | age         | 1                             |
      | type        | SYNTHESIZER                   |
      | family      | KEYBOARD                      |
      | photos      | /photos/synth.jpg             |
    Then both instruments should be successfully registered
    And I should be able to search for "Fender" and find 1 instrument
    And I should be able to search for "Korg" and find 1 instrument

  Scenario: Register an instrument and verify all details are saved
    Given I am an instrument owner with ID 2
    When I register an instrument with the following details:
      | name        | Yamaha C40                    |
      | description | Classical nylon string guitar |
      | price       | 199.99                        |
      | age         | 0                             |
      | type        | ACOUSTIC                      |
      | family      | GUITAR                        |
      | photos      | /photos/yamaha_c40.jpg        |
    Then the instrument should be successfully registered
    And the registered instrument should have all the details I provided
