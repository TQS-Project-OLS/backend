package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MusicSheetRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    private MusicSheet moonlightSonata;
    private MusicSheet bohemianRhapsody;
    private MusicSheet autumnLeaves;

    @BeforeEach
    void setUp() {
        musicSheetRepository.deleteAll();

        moonlightSonata = new MusicSheet();
        moonlightSonata.setName("Moonlight Sonata");
        moonlightSonata.setComposer("Beethoven");
        moonlightSonata.setCategory("CLASSICAL");
        moonlightSonata.setDescription("Piano Sonata No. 14");
        moonlightSonata.setOwnerId(1);
        moonlightSonata.setPrice(9.99);

        bohemianRhapsody = new MusicSheet();
        bohemianRhapsody.setName("Bohemian Rhapsody");
        bohemianRhapsody.setComposer("Freddie Mercury");
        bohemianRhapsody.setCategory("ROCK");
        bohemianRhapsody.setDescription("Queen masterpiece");
        bohemianRhapsody.setOwnerId(1);
        bohemianRhapsody.setPrice(12.99);

        autumnLeaves = new MusicSheet();
        autumnLeaves.setName("Autumn Leaves");
        autumnLeaves.setComposer("Joseph Kosma");
        autumnLeaves.setCategory("JAZZ");
        autumnLeaves.setDescription("Jazz standard");
        autumnLeaves.setOwnerId(2);
        autumnLeaves.setPrice(7.99);
    }

    @Test
    void testFindByNameContainingIgnoreCase_WithExactMatch_ShouldReturnOneSheet() {
        // Arrange
        entityManager.persistAndFlush(moonlightSonata);
        entityManager.persistAndFlush(bohemianRhapsody);

        // Act
        List<MusicSheet> result = musicSheetRepository.findByNameContainingIgnoreCase("Moonlight Sonata");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Moonlight Sonata", result.get(0).getName());
        assertEquals("Beethoven", result.get(0).getComposer());
    }

    @Test
    void testFindByNameContainingIgnoreCase_WithPartialMatch_ShouldReturnSheet() {
        // Arrange
        entityManager.persistAndFlush(moonlightSonata);
        entityManager.persistAndFlush(bohemianRhapsody);
        entityManager.persistAndFlush(autumnLeaves);

        // Act
        List<MusicSheet> result = musicSheetRepository.findByNameContainingIgnoreCase("Sonata");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Moonlight Sonata", result.get(0).getName());
    }

    @Test
    void testFindByNameContainingIgnoreCase_WithNoMatch_ShouldReturnEmptyList() {
        // Arrange
        entityManager.persistAndFlush(moonlightSonata);

        // Act
        List<MusicSheet> result = musicSheetRepository.findByNameContainingIgnoreCase("Symphony");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByNameContainingIgnoreCase_CaseInsensitive_ShouldReturnSheets() {
        // Arrange
        entityManager.persistAndFlush(moonlightSonata);
        entityManager.persistAndFlush(bohemianRhapsody);

        // Act - test case insensitivity with one example
        List<MusicSheet> result = musicSheetRepository.findByNameContainingIgnoreCase("moonlight");

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void testFindByCategory_WithMatchingResults_ShouldReturnSheets() {
        // Arrange
        MusicSheet furElise = new MusicSheet();
        furElise.setName("Fur Elise");
        furElise.setComposer("Beethoven");
        furElise.setCategory("CLASSICAL");
        furElise.setOwnerId(1);
        furElise.setPrice(8.99);

        entityManager.persistAndFlush(moonlightSonata);
        entityManager.persistAndFlush(furElise);
        entityManager.persistAndFlush(bohemianRhapsody);

        // Act
        List<MusicSheet> result = musicSheetRepository.findByCategory("CLASSICAL");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> "CLASSICAL".equals(s.getCategory())));
        assertTrue(result.stream().anyMatch(s -> s.getName().equals("Moonlight Sonata")));
        assertTrue(result.stream().anyMatch(s -> s.getName().equals("Fur Elise")));
    }

    @Test
    void testFindByCategory_WithNoMatch_ShouldReturnEmptyList() {
        // Arrange
        entityManager.persistAndFlush(moonlightSonata);
        entityManager.persistAndFlush(bohemianRhapsody);

        // Act
        List<MusicSheet> result = musicSheetRepository.findByCategory("POP");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByCategory_WithSingleResult_ShouldReturnOneSheet() {
        // Arrange
        entityManager.persistAndFlush(moonlightSonata);
        entityManager.persistAndFlush(bohemianRhapsody);
        entityManager.persistAndFlush(autumnLeaves);

        // Act
        List<MusicSheet> result = musicSheetRepository.findByCategory("JAZZ");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Autumn Leaves", result.get(0).getName());
        assertEquals("JAZZ", result.get(0).getCategory());
    }

    @Test
    void testFindByCategory_VerifyEnumPersistence() {
        // Arrange
        entityManager.persistAndFlush(moonlightSonata);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<MusicSheet> result = musicSheetRepository.findByCategory("CLASSICAL");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CLASSICAL", result.get(0).getCategory());
        assertNotNull(result.get(0).getCategory());
    }

    @Test
    void testSaveAndRetrieve_ShouldPersistCorrectly() {
        // Act
        MusicSheet saved = musicSheetRepository.save(moonlightSonata);
        entityManager.flush();
        entityManager.clear();

        List<MusicSheet> found = musicSheetRepository.findByNameContainingIgnoreCase("Moonlight Sonata");

        // Assert
        assertNotNull(saved.getId());
        assertEquals(1, found.size());
        assertEquals(saved.getId(), found.get(0).getId());
        assertEquals("Moonlight Sonata", found.get(0).getName());
        assertEquals("Beethoven", found.get(0).getComposer());
        assertEquals(9.99, found.get(0).getPrice());
        assertEquals("CLASSICAL", found.get(0).getCategory());
    }
}
