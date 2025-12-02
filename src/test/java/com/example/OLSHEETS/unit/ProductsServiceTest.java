package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetCategory;
import com.example.OLSHEETS.dto.InstrumentRegistrationRequest;
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
import static org.mockito.ArgumentMatchers.any;

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
        sheet1.setCategory("CLASSICAL");
        sheet1.setPrice(9.99);
        sheet1.setOwnerId(1);

        sheet2 = new MusicSheet();
        sheet2.setId(2L);
        sheet2.setName("Bohemian Rhapsody");
        sheet2.setComposer("Freddie Mercury");
        sheet2.setCategory("ROCK");
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
        String category = "CLASSICAL";
        List<MusicSheet> sheets = Collections.singletonList(sheet1);

        when(musicSheetRepository.findByCategory(category)).thenReturn(sheets);

        List<MusicSheet> result = productsService.filterMusicSheetsByCategory(category);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CLASSICAL", result.get(0).getCategory());
        verify(musicSheetRepository, times(1)).findByCategory(category);
    }

    @Test
    void testFilterMusicSheetsByCategory_WithNoResults_ShouldReturnEmptyList() {
        String category = "JAZZ";
        List<MusicSheet> emptyList = Collections.emptyList();

        when(musicSheetRepository.findByCategory(category)).thenReturn(emptyList);

        List<MusicSheet> result = productsService.filterMusicSheetsByCategory(category);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(musicSheetRepository, times(1)).findByCategory(category);
    }

    @Test
    void testRegisterInstrument_WithValidData_ShouldReturnSavedInstrument() {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Gibson Les Paul");
        request.setDescription("Classic electric guitar in excellent condition");
        request.setPrice(1499.99);
        request.setOwnerId(5);
        request.setAge(3);
        request.setType(InstrumentType.ELECTRIC);
        request.setFamily(InstrumentFamily.GUITAR);
        request.setPhotoPaths(Arrays.asList("/photos/guitar1.jpg", "/photos/guitar2.jpg"));

        Instrument savedInstrument = new Instrument();
        savedInstrument.setId(10L);
        savedInstrument.setName(request.getName());
        savedInstrument.setDescription(request.getDescription());
        savedInstrument.setPrice(request.getPrice());
        savedInstrument.setOwnerId(request.getOwnerId());
        savedInstrument.setAge(request.getAge());
        savedInstrument.setType(request.getType());
        savedInstrument.setFamily(request.getFamily());

        when(instrumentRepository.save(any(Instrument.class))).thenReturn(savedInstrument);

        Instrument result = productsService.registerInstrument(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Gibson Les Paul", result.getName());
        assertEquals("Classic electric guitar in excellent condition", result.getDescription());
        assertEquals(1499.99, result.getPrice());
        assertEquals(5, result.getOwnerId());
        assertEquals(3, result.getAge());
        assertEquals(InstrumentType.ELECTRIC, result.getType());
        assertEquals(InstrumentFamily.GUITAR, result.getFamily());
        verify(instrumentRepository, times(1)).save(any(Instrument.class));
    }

    @Test
    void testRegisterInstrument_WithPhotos_ShouldCreateFileReferences() {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Fender Jazz Bass");
        request.setDescription("Professional bass guitar");
        request.setPrice(999.99);
        request.setOwnerId(3);
        request.setAge(2);
        request.setType(InstrumentType.BASS);
        request.setFamily(InstrumentFamily.GUITAR);
        request.setPhotoPaths(Arrays.asList("/photos/bass1.jpg", "/photos/bass2.jpg", "/photos/bass3.jpg"));

        Instrument savedInstrument = new Instrument();
        savedInstrument.setId(11L);
        savedInstrument.setName(request.getName());

        when(instrumentRepository.save(any(Instrument.class))).thenAnswer(invocation -> {
            Instrument arg = invocation.getArgument(0);
            arg.setId(11L);
            return arg;
        });

        Instrument result = productsService.registerInstrument(request);

        assertNotNull(result);
        assertNotNull(result.getFileReferences());
        assertEquals(3, result.getFileReferences().size());
        verify(instrumentRepository, times(1)).save(any(Instrument.class));
    }

    @Test
    void testRegisterInstrument_WithoutPhotos_ShouldSaveInstrument() {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Roland TD-17");
        request.setDescription("Electronic drum kit");
        request.setPrice(1299.99);
        request.setOwnerId(7);
        request.setAge(1);
        request.setType(InstrumentType.DRUMS);
        request.setFamily(InstrumentFamily.PERCUSSION);
        request.setPhotoPaths(null);

        Instrument savedInstrument = new Instrument();
        savedInstrument.setId(12L);
        savedInstrument.setName(request.getName());
        savedInstrument.setDescription(request.getDescription());
        savedInstrument.setPrice(request.getPrice());
        savedInstrument.setOwnerId(request.getOwnerId());
        savedInstrument.setAge(request.getAge());
        savedInstrument.setType(request.getType());
        savedInstrument.setFamily(request.getFamily());

        when(instrumentRepository.save(any(Instrument.class))).thenReturn(savedInstrument);

        Instrument result = productsService.registerInstrument(request);

        assertNotNull(result);
        assertEquals("Roland TD-17", result.getName());
        assertEquals("Electronic drum kit", result.getDescription());
        verify(instrumentRepository, times(1)).save(any(Instrument.class));
    }

    @Test
    void testRegisterInstrument_WithEmptyPhotoList_ShouldSaveInstrument() {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Korg Minilogue");
        request.setDescription("Analog synthesizer");
        request.setPrice(649.99);
        request.setOwnerId(2);
        request.setAge(1);
        request.setType(InstrumentType.SYNTHESIZER);
        request.setFamily(InstrumentFamily.KEYBOARD);
        request.setPhotoPaths(Collections.emptyList());

        Instrument savedInstrument = new Instrument();
        savedInstrument.setId(13L);
        savedInstrument.setName(request.getName());

        when(instrumentRepository.save(any(Instrument.class))).thenReturn(savedInstrument);

        Instrument result = productsService.registerInstrument(request);

        assertNotNull(result);
        assertEquals("Korg Minilogue", result.getName());
        verify(instrumentRepository, times(1)).save(any(Instrument.class));
    }

    @Test
    void testRegisterInstrument_WithSinglePhoto_ShouldCreateOneFileReference() {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Taylor 814ce");
        request.setDescription("Acoustic guitar");
        request.setPrice(3299.99);
        request.setOwnerId(4);
        request.setAge(0);
        request.setType(InstrumentType.ACOUSTIC);
        request.setFamily(InstrumentFamily.GUITAR);
        request.setPhotoPaths(Collections.singletonList("/photos/taylor.jpg"));

        when(instrumentRepository.save(any(Instrument.class))).thenAnswer(invocation -> {
            Instrument arg = invocation.getArgument(0);
            arg.setId(14L);
            return arg;
        });

        Instrument result = productsService.registerInstrument(request);

        assertNotNull(result);
        assertNotNull(result.getFileReferences());
        assertEquals(1, result.getFileReferences().size());
        assertEquals("photo", result.getFileReferences().get(0).getType());
        assertEquals("/photos/taylor.jpg", result.getFileReferences().get(0).getPath());
        verify(instrumentRepository, times(1)).save(any(Instrument.class));
    }
}
