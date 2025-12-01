Feature: Search for music sheets by music title
  As a sheet renter
  I want to search for music sheets by music title
  So that I can find the specific sheet I want to learn

  Scenario: Search music sheets by exact name match
    Given the following music sheets exist:
      | name             | composer         | category  | price | description          |
      | Moonlight Sonata | Beethoven        | CLASSICAL | 9.99  | Piano Sonata No. 14  |
      | Fur Elise        | Beethoven        | CLASSICAL | 8.99  | Bagatelle No. 25     |
      | Autumn Leaves    | Joseph Kosma     | JAZZ      | 7.99  | Jazz standard        |
    When I search for music sheets with name "Moonlight Sonata"
    Then I should receive 1 music sheet
    And the first music sheet should have name "Moonlight Sonata"

  Scenario: Search music sheets by partial name match
    Given the following music sheets exist:
      | name             | composer         | category  | price | description          |
      | Moonlight Sonata | Beethoven        | CLASSICAL | 9.99  | Piano Sonata No. 14  |
      | Fur Elise        | Beethoven        | CLASSICAL | 8.99  | Bagatelle No. 25     |
      | Autumn Leaves    | Joseph Kosma     | JAZZ      | 7.99  | Jazz standard        |
    When I search for music sheets with name "Sonata"
    Then I should receive 1 music sheet

  Scenario: Search music sheets with no matches
    Given the following music sheets exist:
      | name             | composer         | category  | price | description          |
      | Moonlight Sonata | Beethoven        | CLASSICAL | 9.99  | Piano Sonata No. 14  |
    When I search for music sheets with name "Symphony"
    Then I should receive 0 music sheets

  Scenario: Search is case insensitive
    Given the following music sheets exist:
      | name             | composer         | category  | price | description          |
      | Moonlight Sonata | Beethoven        | CLASSICAL | 9.99  | Piano Sonata No. 14  |
    When I search for music sheets with name "moonlight"
    Then I should receive 1 music sheet
