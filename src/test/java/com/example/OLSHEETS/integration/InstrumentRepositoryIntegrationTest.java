package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.entity.InstrumentEntity;
import com.example.OLSHEETS.repository.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class InstrumentRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InstrumentRepository instrumentRepository;

    private InstrumentEntity yamahaPiano;
    private InstrumentEntity fenderGuitar;
    private InstrumentEntity yamahaSax;

    @BeforeEach
    void setUp() {
        instrumentRepository.deleteAll();

        yamahaPiano = new InstrumentEntity();
        yamahaPiano.setName("Yamaha P-125");
        yamahaPiano.setDescription("Digital Piano");
        yamahaPiano.setOwner_id(1);
        yamahaPiano.setPrice(599.99);
        yamahaPiano.setAge(2);
        yamahaPiano.setType("Digital Piano");
        yamahaPiano.setFamily("Keyboard");

        fenderGuitar = new InstrumentEntity();
        fenderGuitar.setName("Fender Stratocaster");
        fenderGuitar.setDescription("Electric Guitar");
        fenderGuitar.setOwner_id(1);
        fenderGuitar.setPrice(899.99);
        fenderGuitar.setAge(5);
        fenderGuitar.setType("Electric");
        fenderGuitar.setFamily("Guitar");

        yamahaSax = new InstrumentEntity();
        yamahaSax.setName("Yamaha YAS-280");
        yamahaSax.setDescription("Alto Saxophone");
        yamahaSax.setOwner_id(2);
        yamahaSax.setPrice(1299.99);
        yamahaSax.setAge(1);
        yamahaSax.setType("Alto Sax");
        yamahaSax.setFamily("Woodwind");
    }

    @Test
    void testFindByNameContainingIgnoreCase_WithExactMatch_ShouldReturnOneInstrument() {
        // Arrange
        entityManager.persistAndFlush(yamahaPiano);
        entityManager.persistAndFlush(fenderGuitar);

        // Act
        List<InstrumentEntity> result = instrumentRepository.findByNameContainingIgnoreCase("Yamaha P-125");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Yamaha P-125", result.get(0).getName());
    }

    @Test
    void testFindByNameContainingIgnoreCase_WithPartialMatch_ShouldReturnMultipleInstruments() {
        // Arrange
        entityManager.persistAndFlush(yamahaPiano);
        entityManager.persistAndFlush(fenderGuitar);
        entityManager.persistAndFlush(yamahaSax);

        // Act
        List<InstrumentEntity> result = instrumentRepository.findByNameContainingIgnoreCase("Yamaha");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(i -> i.getName().equals("Yamaha P-125")));
        assertTrue(result.stream().anyMatch(i -> i.getName().equals("Yamaha YAS-280")));
    }

    @Test
    void testFindByNameContainingIgnoreCase_WithNoMatch_ShouldReturnEmptyList() {
        // Arrange
        entityManager.persistAndFlush(yamahaPiano);

        // Act
        List<InstrumentEntity> result = instrumentRepository.findByNameContainingIgnoreCase("Gibson");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByNameContainingIgnoreCase_CaseInsensitive_ShouldReturnInstruments() {
        // Arrange
        entityManager.persistAndFlush(yamahaPiano);
        entityManager.persistAndFlush(yamahaSax);

        // Act
        List<InstrumentEntity> resultLowerCase = instrumentRepository.findByNameContainingIgnoreCase("yamaha");
        List<InstrumentEntity> resultUpperCase = instrumentRepository.findByNameContainingIgnoreCase("YAMAHA");
        List<InstrumentEntity> resultMixedCase = instrumentRepository.findByNameContainingIgnoreCase("YaMaHa");

        // Assert
        assertEquals(2, resultLowerCase.size());
        assertEquals(2, resultUpperCase.size());
        assertEquals(2, resultMixedCase.size());
    }

    @Test
    void testFindByNameContainingIgnoreCase_WithPartialWord_ShouldReturnInstruments() {
        // Arrange
        entityManager.persistAndFlush(fenderGuitar);

        // Act
        List<InstrumentEntity> result = instrumentRepository.findByNameContainingIgnoreCase("Strat");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Fender Stratocaster", result.get(0).getName());
    }

    @Test
    void testFindByNameContainingIgnoreCase_VerifyDiscriminatorColumn() {
        // Arrange - Verify that only instruments are returned (not other item types)
        entityManager.persistAndFlush(yamahaPiano);
        entityManager.flush();

        // Act
        List<InstrumentEntity> result = instrumentRepository.findByNameContainingIgnoreCase("Yamaha");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        // Verify instrument-specific fields are populated
        assertNotNull(result.get(0).getAge());
        assertNotNull(result.get(0).getType());
        assertNotNull(result.get(0).getFamily());
    }

    @Test
    void testSaveAndRetrieve_ShouldPersistCorrectly() {
        // Act
        InstrumentEntity saved = instrumentRepository.save(yamahaPiano);
        entityManager.flush();
        entityManager.clear();

        List<InstrumentEntity> found = instrumentRepository.findByNameContainingIgnoreCase("Yamaha P-125");

        // Assert
        assertNotNull(saved.getId());
        assertEquals(1, found.size());
        assertEquals(saved.getId(), found.get(0).getId());
        assertEquals("Yamaha P-125", found.get(0).getName());
        assertEquals(599.99, found.get(0).getPrice());
        assertEquals(2, found.get(0).getAge());
        assertEquals("Keyboard", found.get(0).getFamily());
    }
}
