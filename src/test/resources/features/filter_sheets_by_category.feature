Feature: Filter music sheets by category
  As a sheet renter
  I want to filter music sheets by category (e.g., classical, rock, jazz)
  So that I can find styles I like

  Scenario: Filter music sheets by CLASSICAL category with multiple matches
    Given the following music sheets exist for category filter:
      | name             | composer         | category  | price | description          |
      | Moonlight Sonata | Beethoven        | CLASSICAL | 9.99  | Piano Sonata No. 14  |
      | Fur Elise        | Beethoven        | CLASSICAL | 8.99  | Bagatelle No. 25     |
      | Autumn Leaves    | Joseph Kosma     | JAZZ      | 7.99  | Jazz standard        |
    When I filter music sheets by category "CLASSICAL"
    Then the filter should return 2 music sheets
    And all filtered music sheets should have category "CLASSICAL"

  Scenario: Filter music sheets by category with single match
    Given the following music sheets exist for category filter:
      | name              | composer         | category  | price | description          |
      | Bohemian Rhapsody | Freddie Mercury  | ROCK      | 12.99 | Queen masterpiece    |
      | Autumn Leaves     | Joseph Kosma     | JAZZ      | 7.99  | Jazz standard        |
    When I filter music sheets by category "ROCK"
    Then the filter should return 1 music sheet
    And the first filtered music sheet should have name "Bohemian Rhapsody"

  Scenario: Filter music sheets by category with no matches
    Given the following music sheets exist for category filter:
      | name             | composer         | category  | price | description          |
      | Moonlight Sonata | Beethoven        | CLASSICAL | 9.99  | Piano Sonata No. 14  |
      | Autumn Leaves    | Joseph Kosma     | JAZZ      | 7.99  | Jazz standard        |
    When I filter music sheets by category "POP"
    Then the filter should return 0 music sheets

  Scenario: Filter music sheets by JAZZ category
    Given the following music sheets exist for category filter:
      | name               | composer         | category  | price | description          |
      | Autumn Leaves      | Joseph Kosma     | JAZZ      | 7.99  | Jazz standard        |
      | Take Five          | Paul Desmond     | JAZZ      | 8.99  | Jazz classic         |
      | Moonlight Sonata   | Beethoven        | CLASSICAL | 9.99  | Piano Sonata No. 14  |
    When I filter music sheets by category "JAZZ"
    Then the filter should return 2 music sheets
    And all filtered music sheets should have category "JAZZ"
