package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.entity.InstrumentEntity;
import com.example.OLSHEETS.mapper.InstrumentMapper;
import com.example.OLSHEETS.model.Instrument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstrumentMapperTest {

    private InstrumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InstrumentMapper();
    }

    @Test
    void testToModel_WithValidEntity_ShouldReturnModel() {
        // create entity
        InstrumentEntity entity = new InstrumentEntity();
        entity.setId(1L);
        entity.setName("Yamaha P-125");
        entity.setDescription("Digital Piano");
        entity.setOwner_id(123);
        entity.setPrice(599.99);
        entity.setAge(2);
        entity.setType("Digital Piano");
        entity.setFamily("Keyboard");

        Instrument result = mapper.toModel(entity);

        // validate instrument creation
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Yamaha P-125", result.getName());
        assertEquals("Digital Piano", result.getDescription());
        assertEquals(123, result.getOwnerId());
        assertEquals(599.99, result.getPrice());
        assertEquals(2, result.getAge());
        assertEquals("Digital Piano", result.getType());
        assertEquals("Keyboard", result.getFamily());
    }

    @Test
    void testToModel_WithNullEntity_ShouldReturnNull() {
        // test null entity
        Instrument result = mapper.toModel(null);
        assertNull(result);
    }

    @Test
    void testToModelList_WithMultipleEntities_ShouldReturnModelList() {
        InstrumentEntity entity1 = new InstrumentEntity();
        entity1.setId(1L);
        entity1.setName("Piano");
        entity1.setPrice(500.0);
        entity1.setOwner_id(1);

        InstrumentEntity entity2 = new InstrumentEntity();
        entity2.setId(2L);
        entity2.setName("Guitar");
        entity2.setPrice(300.0);
        entity2.setOwner_id(2);

        // test list mapping
        List<InstrumentEntity> entities = Arrays.asList(entity1, entity2);
        List<Instrument> result = mapper.toModelList(entities);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Piano", result.get(0).getName());
        assertEquals("Guitar", result.get(1).getName());
    }

    @Test
    void testToModelList_WithEmptyList_ShouldReturnEmptyList() {
        List<Instrument> result = mapper.toModelList(Arrays.asList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
