package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetCategory;
import com.example.OLSHEETS.repository.InstrumentRepository;
import com.example.OLSHEETS.repository.MusicSheetRepository;
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
    private MusicSheetRepository musicSheetRepository;

    @InjectMocks
    private ProductsService productsService;

    private Instrument instrument1;
    private Instrument instrument2;
    private MusicSheet sheet1;
    private MusicSheet sheet2;

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

        sheet1 = new MusicSheet();
        sheet1.setId(1L);
        sheet1.setName("Moonlight Sonata");
        sheet1.setComposer("Beethoven");
        sheet1.setCategory(SheetCategory.CLASSICAL);
        sheet1.setPrice(9.99);
        sheet1.setOwnerId(1);

        sheet2 = new MusicSheet();
        sheet2.setId(2L);
        sheet2.setName("Bohemian Rhapsody");
        sheet2.setComposer("Freddie Mercury");
        sheet2.setCategory(SheetCategory.ROCK);
        sheet2.setPrice(12.99);
        sheet2.setOwnerId(1);
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
    void testFilterInstrumentsByFamily_WithMatchingResults_ShouldReturnInstruments() {
        InstrumentFamily family = InstrumentFamily.KEYBOARD;
        List<Instrument> instruments = Collections.singletonList(instrument1);

        when(instrumentRepository.findByFamily(family)).thenReturn(instruments);

        List<Instrument> result = productsService.filterInstrumentsByFamily(family);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(instrumentRepository, times(1)).findByFamily(family);
    }

    @Test
    void testFilterInstrumentsByFamily_WithNoResults_ShouldReturnEmptyList() {
        InstrumentFamily family = InstrumentFamily.BRASS;
        List<Instrument> emptyList = Collections.emptyList();

        when(instrumentRepository.findByFamily(family)).thenReturn(emptyList);

        List<Instrument> result = productsService.filterInstrumentsByFamily(family);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(instrumentRepository, times(1)).findByFamily(family);
    }

    @Test
    void testSearchMusicSheetsByName_WithMatchingResults_ShouldReturnSheets() {
        String searchName = "Sonata";
        List<MusicSheet> sheets = Collections.singletonList(sheet1);

        when(musicSheetRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(sheets);

        List<MusicSheet> result = productsService.searchMusicSheetsByName(searchName);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Moonlight Sonata", result.get(0).getName());
        assertEquals("Beethoven", result.get(0).getComposer());
        verify(musicSheetRepository, times(1)).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    void testSearchMusicSheetsByName_WithNoResults_ShouldReturnEmptyList() {
        String searchName = "Symphony";
        List<MusicSheet> emptyList = Collections.emptyList();

        when(musicSheetRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(emptyList);

        List<MusicSheet> result = productsService.searchMusicSheetsByName(searchName);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(musicSheetRepository, times(1)).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    void testSearchMusicSheetsByName_WithMultipleResults_ShouldReturnAllSheets() {
        String searchName = "Rhapsody";
        List<MusicSheet> sheets = Arrays.asList(sheet2);

        when(musicSheetRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(sheets);

        List<MusicSheet> result = productsService.searchMusicSheetsByName(searchName);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Bohemian Rhapsody", result.get(0).getName());
        verify(musicSheetRepository, times(1)).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    void testFilterMusicSheetsByCategory_WithMatchingResults_ShouldReturnSheets() {
        SheetCategory category = SheetCategory.CLASSICAL;
        List<MusicSheet> sheets = Collections.singletonList(sheet1);

        when(musicSheetRepository.findByCategory(category)).thenReturn(sheets);

        List<MusicSheet> result = productsService.filterMusicSheetsByCategory(category);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(SheetCategory.CLASSICAL, result.get(0).getCategory());
        verify(musicSheetRepository, times(1)).findByCategory(category);
    }

    @Test
    void testFilterMusicSheetsByCategory_WithNoResults_ShouldReturnEmptyList() {
        SheetCategory category = SheetCategory.JAZZ;
        List<MusicSheet> emptyList = Collections.emptyList();

        when(musicSheetRepository.findByCategory(category)).thenReturn(emptyList);

        List<MusicSheet> result = productsService.filterMusicSheetsByCategory(category);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(musicSheetRepository, times(1)).findByCategory(category);
    }
}
