package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
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

    @InjectMocks
    private ProductsService productsService;

    private Instrument instrument1;
    private Instrument instrument2;

    @BeforeEach
    void setUp() {
        instrument1 = new Instrument();
        instrument1.setId(1L);
        instrument1.setName("Yamaha P-125");
        instrument1.setPrice(599.99);
        instrument1.setOwnerId(1);

        instrument2 = new Instrument();
        instrument2.setId(2L);
        instrument2.setName("Yamaha YAS-280");
        instrument2.setPrice(1299.99);
        instrument2.setOwnerId(1);
    }

    @Test
    void testSearchInstrumentsByName_WithMatchingResults_ShouldReturnInstruments() {
        String searchName = "Yamaha";
        List<Instrument> instruments = Arrays.asList(instrument1, instrument2);

        when(instrumentRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(instruments);

        List<Instrument> result = productsService.searchInstrumentsByName(searchName);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Yamaha P-125", result.get(0).getName());
        assertEquals("Yamaha YAS-280", result.get(1).getName());
        verify(instrumentRepository, times(1)).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    void testSearchInstrumentsByName_WithNoResults_ShouldReturnEmptyList() {
        String searchName = "Gibson";
        List<Instrument> emptyList = Collections.emptyList();

        when(instrumentRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(emptyList);

        List<Instrument> result = productsService.searchInstrumentsByName(searchName);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(instrumentRepository, times(1)).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    void testSearchInstrumentsByName_WithSingleResult_ShouldReturnOneInstrument() {
        String searchName = "Yamaha P-125";
        List<Instrument> instruments = Collections.singletonList(instrument1);

        when(instrumentRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(instruments);

        List<Instrument> result = productsService.searchInstrumentsByName(searchName);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Yamaha P-125", result.get(0).getName());
        verify(instrumentRepository, times(1)).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    void testSearchInstrumentsByName_VerifiesIgnoreCase() {
        String searchName = "yamaha";
        List<Instrument> instruments = Arrays.asList(instrument1, instrument2);

        when(instrumentRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(instruments);

        List<Instrument> result = productsService.searchInstrumentsByName(searchName);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(instrumentRepository, times(1)).findByNameContainingIgnoreCase("yamaha");
    }

    @Test
    void testFilterInstrumentsByType_WithMatchingResults_ShouldReturnInstruments() {
        Instrument electricGuitar = new Instrument();
        electricGuitar.setId(3L);
        electricGuitar.setName("Fender Stratocaster");
        electricGuitar.setType(InstrumentType.ELECTRIC);
        electricGuitar.setPrice(899.99);

        Instrument electricBass = new Instrument();
        electricBass.setId(4L);
        electricBass.setName("Fender Precision Bass");
        electricBass.setType(InstrumentType.ELECTRIC);
        electricBass.setPrice(799.99);

        List<Instrument> electricInstruments = Arrays.asList(electricGuitar, electricBass);

        when(instrumentRepository.findByType(InstrumentType.ELECTRIC)).thenReturn(electricInstruments);

        List<Instrument> result = productsService.filterInstrumentsByType(InstrumentType.ELECTRIC);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(InstrumentType.ELECTRIC, result.get(0).getType());
        assertEquals(InstrumentType.ELECTRIC, result.get(1).getType());
        verify(instrumentRepository, times(1)).findByType(InstrumentType.ELECTRIC);
    }

    @Test
    void testFilterInstrumentsByType_WithNoResults_ShouldReturnEmptyList() {
        List<Instrument> emptyList = Collections.emptyList();

        when(instrumentRepository.findByType(InstrumentType.SYNTHESIZER)).thenReturn(emptyList);

        List<Instrument> result = productsService.filterInstrumentsByType(InstrumentType.SYNTHESIZER);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(instrumentRepository, times(1)).findByType(InstrumentType.SYNTHESIZER);
    }
}
