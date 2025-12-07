package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
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

    private Instrument yamahaPiano;
    private Instrument fenderGuitar;
    private Instrument yamahaSax;

    @BeforeEach
    void setUp() {
        instrumentRepository.deleteAll();

        yamahaPiano = new Instrument();
        yamahaPiano.setName("Yamaha P-125");
        yamahaPiano.setDescription("Digital Piano");
        com.example.OLSHEETS.data.User owner1 = new com.example.OLSHEETS.data.User("owner1");
        owner1.setId(1L);
        yamahaPiano.setOwner(owner1);
        yamahaPiano.setPrice(599.99);
        yamahaPiano.setAge(2);
        yamahaPiano.setType(InstrumentType.DIGITAL);
        yamahaPiano.setFamily(InstrumentFamily.KEYBOARD);

        fenderGuitar = new Instrument();
        fenderGuitar.setName("Fender Stratocaster");
        fenderGuitar.setDescription("Electric Guitar");
        com.example.OLSHEETS.data.User owner1b = new com.example.OLSHEETS.data.User("owner1");
        owner1b.setId(1L);
        fenderGuitar.setOwner(owner1b);
        fenderGuitar.setPrice(899.99);
        fenderGuitar.setAge(5);
        fenderGuitar.setType(InstrumentType.ELECTRIC);
        fenderGuitar.setFamily(InstrumentFamily.GUITAR);

        yamahaSax = new Instrument();
        yamahaSax.setName("Yamaha YAS-280");
        yamahaSax.setDescription("Alto Saxophone");
        com.example.OLSHEETS.data.User owner2 = new com.example.OLSHEETS.data.User("owner2");
        owner2.setId(2L);
        yamahaSax.setOwner(owner2);
        yamahaSax.setPrice(1299.99);
        yamahaSax.setAge(1);
        yamahaSax.setType(InstrumentType.WIND);
        yamahaSax.setFamily(InstrumentFamily.WOODWIND);
    }

    @Test
    void testFindByNameContainingIgnoreCase_WithExactMatch_ShouldReturnOneInstrument() {
        // Arrange
        entityManager.persistAndFlush(yamahaPiano);
        entityManager.persistAndFlush(fenderGuitar);

        // Act
        List<Instrument> result = instrumentRepository.findByNameContainingIgnoreCase("Yamaha P-125");

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
        List<Instrument> result = instrumentRepository.findByNameContainingIgnoreCase("Yamaha");

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
        List<Instrument> result = instrumentRepository.findByNameContainingIgnoreCase("Gibson");

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
        List<Instrument> resultLowerCase = instrumentRepository.findByNameContainingIgnoreCase("yamaha");
        List<Instrument> resultUpperCase = instrumentRepository.findByNameContainingIgnoreCase("YAMAHA");
        List<Instrument> resultMixedCase = instrumentRepository.findByNameContainingIgnoreCase("YaMaHa");

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
        List<Instrument> result = instrumentRepository.findByNameContainingIgnoreCase("Strat");

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
        List<Instrument> result = instrumentRepository.findByNameContainingIgnoreCase("Yamaha");

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
        Instrument saved = instrumentRepository.save(yamahaPiano);
        entityManager.flush();
        entityManager.clear();

        List<Instrument> found = instrumentRepository.findByNameContainingIgnoreCase("Yamaha P-125");

        // Assert
        assertNotNull(saved.getId());
        assertEquals(1, found.size());
        assertEquals(saved.getId(), found.get(0).getId());
        assertEquals("Yamaha P-125", found.get(0).getName());
        assertEquals(599.99, found.get(0).getPrice());
        assertEquals(2, found.get(0).getAge());
        assertEquals(InstrumentFamily.KEYBOARD, found.get(0).getFamily());
    }

    @Test
    void testFindByType_WithMatchingResults_ShouldReturnInstruments() {
        // Arrange
        Instrument acousticGuitar1 = new Instrument();
        acousticGuitar1.setName("Martin D-28");
        acousticGuitar1.setType(InstrumentType.ACOUSTIC);
        com.example.OLSHEETS.data.User ownerA = new com.example.OLSHEETS.data.User("owner1");
        ownerA.setId(1L);
        acousticGuitar1.setOwner(ownerA);
        acousticGuitar1.setPrice(2899.99);

        Instrument acousticGuitar2 = new Instrument();
        acousticGuitar2.setName("Taylor 214ce");
        acousticGuitar2.setType(InstrumentType.ACOUSTIC);
        com.example.OLSHEETS.data.User ownerB = new com.example.OLSHEETS.data.User("owner2");
        ownerB.setId(2L);
        acousticGuitar2.setOwner(ownerB);
        acousticGuitar2.setPrice(1099.99);

        entityManager.persistAndFlush(acousticGuitar1);
        entityManager.persistAndFlush(acousticGuitar2);
        entityManager.persistAndFlush(fenderGuitar); // Electric type

        // Act
        List<Instrument> result = instrumentRepository.findByType(InstrumentType.ACOUSTIC);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> i.getType() == InstrumentType.ACOUSTIC));
        assertTrue(result.stream().anyMatch(i -> i.getName().equals("Martin D-28")));
        assertTrue(result.stream().anyMatch(i -> i.getName().equals("Taylor 214ce")));
    }

    @Test
    void testFindByType_WithNoMatch_ShouldReturnEmptyList() {
        // Arrange
        entityManager.persistAndFlush(yamahaPiano);
        entityManager.persistAndFlush(fenderGuitar);

        // Act
        List<Instrument> result = instrumentRepository.findByType(InstrumentType.SYNTHESIZER);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByType_WithSingleResult_ShouldReturnOneInstrument() {
        // Arrange
        entityManager.persistAndFlush(yamahaPiano);
        entityManager.persistAndFlush(fenderGuitar);
        entityManager.persistAndFlush(yamahaSax);

        // Act
        List<Instrument> result = instrumentRepository.findByType(InstrumentType.DIGITAL);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Yamaha P-125", result.get(0).getName());
        assertEquals(InstrumentType.DIGITAL, result.get(0).getType());
    }

    @Test
    void testFindByType_VerifyEnumPersistence() {
        // Arrange
        entityManager.persistAndFlush(yamahaPiano);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Instrument> result = instrumentRepository.findByType(InstrumentType.DIGITAL);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(InstrumentType.DIGITAL, result.get(0).getType());
        // Verify that the enum is stored and retrieved correctly
        assertNotNull(result.get(0).getType());
    }
}
