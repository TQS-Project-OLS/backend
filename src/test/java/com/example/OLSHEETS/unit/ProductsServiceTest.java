package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.entity.InstrumentEntity;
import com.example.OLSHEETS.mapper.InstrumentMapper;
import com.example.OLSHEETS.model.Instrument;
import com.example.OLSHEETS.repository.InstrumentRepository;
import com.example.OLSHEETS.service.ProductsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private InstrumentMapper instrumentMapper;

    @InjectMocks
    private ProductsService productsService;

    private InstrumentEntity entity1;
    private InstrumentEntity entity2;
    private Instrument model1;
    private Instrument model2;

    @BeforeEach
    void setUp() {
        entity1 = new InstrumentEntity();
        entity1.setId(1L);
        entity1.setName("Yamaha P-125");
        entity1.setPrice(599.99);
        entity1.setOwner_id(1);

        entity2 = new InstrumentEntity();
        entity2.setId(2L);
        entity2.setName("Yamaha YAS-280");
        entity2.setPrice(1299.99);
        entity2.setOwner_id(1);

        model1 = new Instrument();
        model1.setId(1L);
        model1.setName("Yamaha P-125");
        model1.setPrice(599.99);
        model1.setOwnerId(1);

        model2 = new Instrument();
        model2.setId(2L);
        model2.setName("Yamaha YAS-280");
        model2.setPrice(1299.99);
        model2.setOwnerId(1);
    }

    @Test
    void testSearchInstrumentsByName_WithMatchingResults_ShouldReturnInstruments() {
        String searchName = "Yamaha";
        List<InstrumentEntity> entities = Arrays.asList(entity1, entity2);
        List<Instrument> expectedModels = Arrays.asList(model1, model2);

        when(instrumentRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(entities);
        when(instrumentMapper.toModelList(entities)).thenReturn(expectedModels);

        List<Instrument> result = productsService.searchInstrumentsByName(searchName);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Yamaha P-125", result.get(0).getName());
        assertEquals("Yamaha YAS-280", result.get(1).getName());
        verify(instrumentRepository, times(1)).findByNameContainingIgnoreCase(searchName);
        verify(instrumentMapper, times(1)).toModelList(entities);
    }

    @Test
    void testSearchInstrumentsByName_WithNoResults_ShouldReturnEmptyList() {
        String searchName = "Gibson";
        List<InstrumentEntity> emptyEntities = Collections.emptyList();
        List<Instrument> emptyModels = Collections.emptyList();

        when(instrumentRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(emptyEntities);
        when(instrumentMapper.toModelList(emptyEntities)).thenReturn(emptyModels);

        List<Instrument> result = productsService.searchInstrumentsByName(searchName);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(instrumentRepository, times(1)).findByNameContainingIgnoreCase(searchName);
        verify(instrumentMapper, times(1)).toModelList(emptyEntities);
    }

    @Test
    void testSearchInstrumentsByName_WithSingleResult_ShouldReturnOneInstrument() {
        // Arrange
        String searchName = "Yamaha P-125";
        List<InstrumentEntity> entities = Collections.singletonList(entity1);
        List<Instrument> expectedModels = Collections.singletonList(model1);

        when(instrumentRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(entities);
        when(instrumentMapper.toModelList(entities)).thenReturn(expectedModels);

        // Act
        List<Instrument> result = productsService.searchInstrumentsByName(searchName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Yamaha P-125", result.get(0).getName());

        verify(instrumentRepository, times(1)).findByNameContainingIgnoreCase(searchName);
        verify(instrumentMapper, times(1)).toModelList(entities);
    }

    @Test
    void testSearchInstrumentsByName_VerifiesIgnoreCase() {
        // Arrange
        String searchName = "yamaha";
        List<InstrumentEntity> entities = Arrays.asList(entity1, entity2);
        List<Instrument> expectedModels = Arrays.asList(model1, model2);

        when(instrumentRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(entities);
        when(instrumentMapper.toModelList(entities)).thenReturn(expectedModels);

        // Act
        List<Instrument> result = productsService.searchInstrumentsByName(searchName);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify the repository method was called with the lowercase search term
        verify(instrumentRepository, times(1)).findByNameContainingIgnoreCase("yamaha");
    }
}
