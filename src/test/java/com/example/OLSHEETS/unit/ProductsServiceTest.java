package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.dto.InstrumentRegistrationRequest;
import com.example.OLSHEETS.dto.MusicSheetRegistrationRequest;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ProductsServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private MusicSheetRepository musicSheetRepository;

    @Mock
    private com.example.OLSHEETS.repository.UserRepository userRepository;

    @InjectMocks
    private ProductsService productsService;

    private Instrument instrument1;
    private Instrument instrument2;
    private MusicSheet sheet1;
    private MusicSheet sheet2;

    @BeforeEach
    void setUp() {
        User owner = new User("owner1", "owner1@example.com", "Owner One", "password123");
        owner.setId(1L);
        instrument1 = new Instrument();
        instrument1.setId(1L);
        instrument1.setName("Yamaha P-125");
        instrument1.setPrice(599.99);
        instrument1.setOwner(owner);

        instrument2 = new Instrument();
        instrument2.setId(2L);
        instrument2.setName("Yamaha YAS-280");
        instrument2.setPrice(1299.99);
        instrument2.setOwner(owner);

        sheet1 = new MusicSheet();
        sheet1.setId(1L);
        sheet1.setName("Moonlight Sonata");
        sheet1.setComposer("Beethoven");
        sheet1.setCategory("CLASSICAL");
        sheet1.setPrice(9.99);
        sheet1.setOwner(owner);

        sheet2 = new MusicSheet();
        sheet2.setId(2L);
        sheet2.setName("Bohemian Rhapsody");
        sheet2.setComposer("Freddie Mercury");
        sheet2.setCategory("ROCK");
        sheet2.setPrice(12.99);
        sheet2.setOwner(owner);
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
    void testGetInstrumentsByOwnerId_WithMatchingResults_ShouldReturnInstruments() {
        Long ownerId = 1L;
        List<Instrument> instruments = Arrays.asList(instrument1, instrument2);

        when(instrumentRepository.findByOwnerId(ownerId)).thenReturn(instruments);

        List<Instrument> result = productsService.getInstrumentsByOwnerId(ownerId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Yamaha P-125", result.get(0).getName());
        assertEquals("Yamaha YAS-280", result.get(1).getName());
        verify(instrumentRepository, times(1)).findByOwnerId(ownerId);
    }

    @Test
    void testGetInstrumentsByOwnerId_WithNoResults_ShouldReturnEmptyList() {
        Long ownerId = 999L;
        List<Instrument> emptyList = Collections.emptyList();

        when(instrumentRepository.findByOwnerId(ownerId)).thenReturn(emptyList);

        List<Instrument> result = productsService.getInstrumentsByOwnerId(ownerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(instrumentRepository, times(1)).findByOwnerId(ownerId);
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

    // Pricing Management Tests

    @Test
    void testUpdateItemPrice_ForInstrument_ShouldUpdateAndReturnItem() {
        Long itemId = 1L;
        Double newPrice = 799.99;
        
        when(instrumentRepository.findById(itemId)).thenReturn(Optional.of(instrument1));
        when(instrumentRepository.save(any(Instrument.class))).thenReturn(instrument1);

        Item result = productsService.updateItemPrice(itemId, newPrice, 1L);

        assertNotNull(result);
        assertEquals(newPrice, result.getPrice());
        verify(instrumentRepository, times(1)).findById(itemId);
        verify(instrumentRepository, times(1)).save(instrument1);
        // musicSheetRepository is only checked if instrumentRepository returns empty
        verify(musicSheetRepository, never()).findById(any());
    }

    @Test
    void testUpdateItemPrice_ForMusicSheet_ShouldUpdateAndReturnItem() {
        Long itemId = 1L;
        Double newPrice = 15.99;
        
        when(instrumentRepository.findById(itemId)).thenReturn(Optional.empty());
        when(musicSheetRepository.findById(itemId)).thenReturn(Optional.of(sheet1));
        when(musicSheetRepository.save(any(MusicSheet.class))).thenReturn(sheet1);

        Item result = productsService.updateItemPrice(itemId, newPrice, 1L);

        assertNotNull(result);
        assertEquals(newPrice, result.getPrice());
        verify(instrumentRepository, times(1)).findById(itemId);
        verify(musicSheetRepository, times(1)).findById(itemId);
        verify(musicSheetRepository, times(1)).save(sheet1);
    }

    @Test
    void testUpdateItemPrice_WithNullPrice_ShouldThrowException() {
        Long itemId = 1L;
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productsService.updateItemPrice(itemId, null, 1L)
        );

        assertEquals("Price must be a positive number", exception.getMessage());
        verify(instrumentRepository, never()).findById(any());
        verify(musicSheetRepository, never()).findById(any());
    }

    @Test
    void testUpdateItemPrice_WithNegativePrice_ShouldThrowException() {
        Long itemId = 1L;
        Double negativePrice = -10.0;
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productsService.updateItemPrice(itemId, negativePrice, 1L)
        );

        assertEquals("Price must be a positive number", exception.getMessage());
        verify(instrumentRepository, never()).findById(any());
        verify(musicSheetRepository, never()).findById(any());
    }

    @Test
    void testUpdateItemPrice_WithZeroPrice_ShouldSucceed() {
        Long itemId = 1L;
        Double zeroPrice = 0.0;
        
        when(instrumentRepository.findById(itemId)).thenReturn(Optional.of(instrument1));
        when(instrumentRepository.save(any(Instrument.class))).thenReturn(instrument1);

        Item result = productsService.updateItemPrice(itemId, zeroPrice, 1L);

        assertNotNull(result);
        assertEquals(zeroPrice, result.getPrice());
        verify(instrumentRepository, times(1)).save(instrument1);
    }

    @Test
    void testUpdateItemPrice_WithNonExistentItem_ShouldThrowException() {
        Long itemId = 999L;
        Double newPrice = 100.0;
        
        when(instrumentRepository.findById(itemId)).thenReturn(Optional.empty());
        when(musicSheetRepository.findById(itemId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productsService.updateItemPrice(itemId, newPrice, 1L)
        );

        assertEquals("Item not found with id: " + itemId, exception.getMessage());
        verify(instrumentRepository, times(1)).findById(itemId);
        verify(musicSheetRepository, times(1)).findById(itemId);
        verify(instrumentRepository, never()).save(any());
        verify(musicSheetRepository, never()).save(any());
    }

    @Test
    void testUpdateItemPrice_WithUnauthorizedUser_ShouldThrowException() {
        Long itemId = 1L;
        Double newPrice = 100.0;
        Long unauthorizedUserId = 999L;
        
        when(instrumentRepository.findById(itemId)).thenReturn(Optional.of(instrument1));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productsService.updateItemPrice(itemId, newPrice, unauthorizedUserId)
        );

        assertEquals("You are not authorized to update the price of this item", exception.getMessage());
        verify(instrumentRepository, times(1)).findById(itemId);
        verify(instrumentRepository, never()).save(any());
        verify(musicSheetRepository, never()).save(any());
    }

    @Test
    void testUpdateItemPrice_WithCorrectOwner_ShouldSucceed() {
        Long itemId = 1L;
        Double newPrice = 999.99;
        Long ownerId = 1L;
        
        when(instrumentRepository.findById(itemId)).thenReturn(Optional.of(instrument1));
        when(instrumentRepository.save(any(Instrument.class))).thenReturn(instrument1);

        Item result = productsService.updateItemPrice(itemId, newPrice, ownerId);

        assertNotNull(result);
        assertEquals(newPrice, result.getPrice());
        verify(instrumentRepository, times(1)).findById(itemId);
        verify(instrumentRepository, times(1)).save(instrument1);
    }

    @Test
    void testUpdateItemPrice_ForMusicSheet_WithUnauthorizedUser_ShouldThrowException() {
        Long itemId = 1L;
        Double newPrice = 20.0;
        Long unauthorizedUserId = 999L;
        
        when(instrumentRepository.findById(itemId)).thenReturn(Optional.empty());
        when(musicSheetRepository.findById(itemId)).thenReturn(Optional.of(sheet1));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productsService.updateItemPrice(itemId, newPrice, unauthorizedUserId)
        );

        assertEquals("You are not authorized to update the price of this item", exception.getMessage());
        verify(instrumentRepository, times(1)).findById(itemId);
        verify(musicSheetRepository, times(1)).findById(itemId);
        verify(musicSheetRepository, never()).save(any());
    }

    @Test
    void testGetItemPrice_ForInstrument_ShouldReturnPrice() {
        Long itemId = 1L;
        
        when(instrumentRepository.findById(itemId)).thenReturn(Optional.of(instrument1));

        Double result = productsService.getItemPrice(itemId);

        assertNotNull(result);
        assertEquals(599.99, result);
        verify(instrumentRepository, times(1)).findById(itemId);
    }

    @Test
    void testGetItemPrice_ForMusicSheet_ShouldReturnPrice() {
        Long itemId = 1L;
        
        when(instrumentRepository.findById(itemId)).thenReturn(Optional.empty());
        when(musicSheetRepository.findById(itemId)).thenReturn(Optional.of(sheet1));

        Double result = productsService.getItemPrice(itemId);

        assertNotNull(result);
        assertEquals(9.99, result);
        verify(instrumentRepository, times(1)).findById(itemId);
        verify(musicSheetRepository, times(1)).findById(itemId);
    }

    @Test
    void testGetItemPrice_WithNonExistentItem_ShouldThrowException() {
        Long itemId = 999L;
        
        when(instrumentRepository.findById(itemId)).thenReturn(Optional.empty());
        when(musicSheetRepository.findById(itemId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productsService.getItemPrice(itemId)
        );

        assertEquals("Item not found with id: " + itemId, exception.getMessage());
        verify(instrumentRepository, times(1)).findById(itemId);
        verify(musicSheetRepository, times(1)).findById(itemId);
    }
    
    @Test
    void testRegisterInstrument_WithValidData_ShouldReturnSavedInstrument() {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Gibson Les Paul");
        request.setDescription("Classic electric guitar in excellent condition");
        request.setPrice(1499.99);
        request.setOwnerId(5L);
        request.setAge(3);
        request.setType(InstrumentType.ELECTRIC);
        request.setFamily(InstrumentFamily.GUITAR);
        request.setPhotoPaths(Arrays.asList("/photos/guitar1.jpg", "/photos/guitar2.jpg"));

        Instrument savedInstrument = new Instrument();
        savedInstrument.setId(10L);
        savedInstrument.setName(request.getName());
        savedInstrument.setDescription(request.getDescription());
        savedInstrument.setPrice(request.getPrice());
        com.example.OLSHEETS.data.User ownerUser = new com.example.OLSHEETS.data.User("owner" + request.getOwnerId(), "owner" +request.getOwnerId() + "@a.com", "owner" + request.getOwnerId());
        ownerUser.setId((long) request.getOwnerId());
        // Mock the userRepository to return the expected owner
        when(userRepository.findById(request.getOwnerId())).thenReturn(java.util.Optional.of(ownerUser));
        savedInstrument.setOwner(ownerUser);
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
        assertEquals(5L, result.getOwner().getId());
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
        request.setOwnerId(3L);
        request.setAge(2);
        request.setType(InstrumentType.BASS);
        request.setFamily(InstrumentFamily.GUITAR);
        request.setPhotoPaths(Arrays.asList("/photos/bass1.jpg", "/photos/bass2.jpg", "/photos/bass3.jpg"));

        Instrument savedInstrument = new Instrument();
        savedInstrument.setId(11L);
        savedInstrument.setName(request.getName());

        com.example.OLSHEETS.data.User ownerUser = new com.example.OLSHEETS.data.User("owner" + request.getOwnerId(), "owner" +request.getOwnerId() + "@a.com", "owner" + request.getOwnerId());
        ownerUser.setId((long) request.getOwnerId());
        when(userRepository.findById(request.getOwnerId())).thenReturn(java.util.Optional.of(ownerUser));

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
        request.setOwnerId(7L);
        request.setAge(1);
        request.setType(InstrumentType.DRUMS);
        request.setFamily(InstrumentFamily.PERCUSSION);
        request.setPhotoPaths(null);

        Instrument savedInstrument = new Instrument();
        savedInstrument.setId(12L);
        savedInstrument.setName(request.getName());
        savedInstrument.setDescription(request.getDescription());
        savedInstrument.setPrice(request.getPrice());
        com.example.OLSHEETS.data.User ownerUser3 = new com.example.OLSHEETS.data.User("owner" + request.getOwnerId(), "owner" +request.getOwnerId() + "@a.com", "owner" + request.getOwnerId());
        ownerUser3.setId((long) request.getOwnerId());
        when(userRepository.findById(request.getOwnerId())).thenReturn(java.util.Optional.of(ownerUser3));
        savedInstrument.setOwner(ownerUser3);
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
        request.setOwnerId(2L);
        request.setAge(1);
        request.setType(InstrumentType.SYNTHESIZER);
        request.setFamily(InstrumentFamily.KEYBOARD);
        request.setPhotoPaths(Collections.emptyList());

        Instrument savedInstrument = new Instrument();
        savedInstrument.setId(13L);
        savedInstrument.setName(request.getName());

        com.example.OLSHEETS.data.User ownerUser = new com.example.OLSHEETS.data.User("owner" + request.getOwnerId(), "owner" +request.getOwnerId() + "@a.com", "owner" + request.getOwnerId());
        ownerUser.setId((long) request.getOwnerId());
        when(userRepository.findById(request.getOwnerId())).thenReturn(java.util.Optional.of(ownerUser));

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
        request.setOwnerId(4L);
        request.setAge(0);
        request.setType(InstrumentType.ACOUSTIC);
        request.setFamily(InstrumentFamily.GUITAR);
        request.setPhotoPaths(Collections.singletonList("/photos/taylor.jpg"));

        com.example.OLSHEETS.data.User ownerUserSingle = new com.example.OLSHEETS.data.User("owner" + request.getOwnerId(), "owner" +request.getOwnerId() + "@a.com", "owner" + request.getOwnerId());
        ownerUserSingle.setId((long) request.getOwnerId());
        when(userRepository.findById(request.getOwnerId())).thenReturn(java.util.Optional.of(ownerUserSingle));

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

    @Test
    void testRegisterMusicSheet_WithValidData_ShouldReturnSavedMusicSheet() {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName("Für Elise");
        request.setDescription("Famous Beethoven composition for piano");
        request.setPrice(7.99);
        request.setOwnerId(5L);
        request.setCategory("CLASSICAL");
        request.setComposer("Ludwig van Beethoven");
        request.setInstrumentation("Piano");
        request.setDuration(3.5f);
        request.setPhotoPaths(Arrays.asList("/photos/sheet1.jpg", "/photos/sheet2.jpg"));

        MusicSheet savedMusicSheet = new MusicSheet();
        savedMusicSheet.setId(20L);
        savedMusicSheet.setName(request.getName());
        savedMusicSheet.setDescription(request.getDescription());
        savedMusicSheet.setPrice(request.getPrice());
        User ownerUser = new User("owner5", "owner5@a.com", "owner5");
        ownerUser.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(ownerUser));
        savedMusicSheet.setOwner(ownerUser);
        savedMusicSheet.setCategory(request.getCategory());
        savedMusicSheet.setComposer(request.getComposer());
        savedMusicSheet.setInstrumentation(request.getInstrumentation());
        savedMusicSheet.setDuration(request.getDuration());

        when(musicSheetRepository.save(any(MusicSheet.class))).thenReturn(savedMusicSheet);

        MusicSheet result = productsService.registerMusicSheet(request);

        assertNotNull(result);
        assertEquals(20L, result.getId());
        assertEquals("Für Elise", result.getName());
        assertEquals("Famous Beethoven composition for piano", result.getDescription());
        assertEquals(7.99, result.getPrice());
        assertEquals(5L, result.getOwner().getId());
        assertEquals("CLASSICAL", result.getCategory());
        assertEquals("Ludwig van Beethoven", result.getComposer());
        assertEquals("Piano", result.getInstrumentation());
        assertEquals(3.5f, result.getDuration());
        verify(musicSheetRepository, times(1)).save(any(MusicSheet.class));
    }

    @Test
    void testRegisterMusicSheet_WithPhotos_ShouldCreateFileReferences() {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName("Moonlight Sonata");
        request.setDescription("Piano Sonata No. 14");
        request.setPrice(9.99);
        request.setOwnerId(3L);
        request.setCategory("CLASSICAL");
        request.setComposer("Beethoven");
        request.setInstrumentation("Piano");
        request.setPhotoPaths(Arrays.asList("/photos/moon1.jpg", "/photos/moon2.jpg", "/photos/moon3.jpg"));

        User ownerUser = new User("owner3", "owner3@a.com", "owner3");
        ownerUser.setId(3L);
        when(userRepository.findById(3L)).thenReturn(Optional.of(ownerUser));

        when(musicSheetRepository.save(any(MusicSheet.class))).thenAnswer(invocation -> {
            MusicSheet arg = invocation.getArgument(0);
            arg.setId(21L);
            return arg;
        });

        MusicSheet result = productsService.registerMusicSheet(request);

        assertNotNull(result);
        assertNotNull(result.getFileReferences());
        assertEquals(3, result.getFileReferences().size());
        verify(musicSheetRepository, times(1)).save(any(MusicSheet.class));
    }

    @Test
    void testRegisterMusicSheet_WithoutPhotos_ShouldSaveMusicSheet() {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName("Claire de Lune");
        request.setDescription("Debussy piano piece");
        request.setPrice(8.99);
        request.setOwnerId(7L);
        request.setCategory("IMPRESSIONIST");
        request.setComposer("Claude Debussy");
        request.setInstrumentation("Piano");
        request.setDuration(4.5f);
        request.setPhotoPaths(null);

        MusicSheet savedMusicSheet = new MusicSheet();
        savedMusicSheet.setId(22L);
        savedMusicSheet.setName(request.getName());
        savedMusicSheet.setDescription(request.getDescription());
        savedMusicSheet.setPrice(request.getPrice());
        User ownerUser = new User("owner7", "owner7@a.com", "owner7");
        ownerUser.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(ownerUser));
        savedMusicSheet.setOwner(ownerUser);
        savedMusicSheet.setCategory(request.getCategory());
        savedMusicSheet.setComposer(request.getComposer());

        when(musicSheetRepository.save(any(MusicSheet.class))).thenReturn(savedMusicSheet);

        MusicSheet result = productsService.registerMusicSheet(request);

        assertNotNull(result);
        assertEquals("Claire de Lune", result.getName());
        assertEquals("Debussy piano piece", result.getDescription());
        verify(musicSheetRepository, times(1)).save(any(MusicSheet.class));
    }

    @Test
    void testRegisterMusicSheet_WithEmptyPhotoList_ShouldSaveMusicSheet() {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName("Autumn Leaves");
        request.setDescription("Jazz standard");
        request.setPrice(7.99);
        request.setOwnerId(2L);
        request.setCategory("JAZZ");
        request.setComposer("Joseph Kosma");
        request.setInstrumentation("Piano");
        request.setPhotoPaths(Collections.emptyList());

        MusicSheet savedMusicSheet = new MusicSheet();
        savedMusicSheet.setId(23L);
        savedMusicSheet.setName(request.getName());

        User ownerUser = new User("owner2", "owner2@a.com", "owner2");
        ownerUser.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(ownerUser));

        when(musicSheetRepository.save(any(MusicSheet.class))).thenReturn(savedMusicSheet);

        MusicSheet result = productsService.registerMusicSheet(request);

        assertNotNull(result);
        assertEquals("Autumn Leaves", result.getName());
        verify(musicSheetRepository, times(1)).save(any(MusicSheet.class));
    }

    @Test
    void testRegisterMusicSheet_WithSinglePhoto_ShouldCreateOneFileReference() {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName("Canon in D");
        request.setDescription("Pachelbel's Canon");
        request.setPrice(6.99);
        request.setOwnerId(4L);
        request.setCategory("BAROQUE");
        request.setComposer("Johann Pachelbel");
        request.setInstrumentation("String Quartet");
        request.setDuration(5.0f);
        request.setPhotoPaths(Collections.singletonList("/photos/canon.jpg"));

        User ownerUser = new User("owner4", "owner4@a.com", "owner4");
        ownerUser.setId(4L);
        when(userRepository.findById(4L)).thenReturn(Optional.of(ownerUser));

        when(musicSheetRepository.save(any(MusicSheet.class))).thenAnswer(invocation -> {
            MusicSheet arg = invocation.getArgument(0);
            arg.setId(24L);
            return arg;
        });

        MusicSheet result = productsService.registerMusicSheet(request);

        assertNotNull(result);
        assertNotNull(result.getFileReferences());
        assertEquals(1, result.getFileReferences().size());
        assertEquals("photo", result.getFileReferences().get(0).getType());
        assertEquals("/photos/canon.jpg", result.getFileReferences().get(0).getPath());
        verify(musicSheetRepository, times(1)).save(any(MusicSheet.class));
    }

    @Test
    void testRegisterMusicSheet_WithNonExistentOwner_ShouldThrowException() {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName("Test Sheet");
        request.setDescription("Test Description");
        request.setPrice(5.99);
        request.setOwnerId(999L);
        request.setCategory("CLASSICAL");
        request.setComposer("Test Composer");
        request.setInstrumentation("Piano");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productsService.registerMusicSheet(request)
        );

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(musicSheetRepository, never()).save(any(MusicSheet.class));
    }
}
