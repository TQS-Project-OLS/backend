package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.dto.MusicSheetRegistrationRequest;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import com.example.OLSHEETS.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
    "spring.main.lazy-initialization=true"
})
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class SheetsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    @Autowired
    private SheetBookingRepository sheetBookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private com.example.OLSHEETS.repository.InstrumentRepository instrumentRepository;

    @Autowired
    private com.example.OLSHEETS.repository.BookingRepository bookingRepository;

    @Autowired
    private com.example.OLSHEETS.repository.UserRepository userRepository;
    
    private com.example.OLSHEETS.data.User testOwner1;

    @BeforeEach
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.test.annotation.Commit
    void setUp() {
        // Make SecurityContext inheritable by child threads (needed for MockMvc)
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        
        // Setup authentication FIRST
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // Delete children before parents to avoid FK constraint issues
        // First delete payments that reference bookings
        paymentRepository.deleteAll();
        // Then delete bookings that reference items
        bookingRepository.deleteAll();
        // Then delete sheet bookings
        sheetBookingRepository.deleteAll();
        // Then delete music sheets
        musicSheetRepository.deleteAll();
        // Also remove any instruments (other Item subtypes) to avoid FK references to users
        instrumentRepository.deleteAll();
        // Finally delete users
        userRepository.deleteAll();

        // Create a testuser for authentication
        testOwner1 = userRepository.save(new com.example.OLSHEETS.data.User("testuser", "testuser@example.com", "Test User", "password123"));
        userRepository.flush();

        MusicSheet moonlightSonata = new MusicSheet();
        moonlightSonata.setName("Moonlight Sonata");
        moonlightSonata.setComposer("Beethoven");
        moonlightSonata.setCategory("CLASSICAL");
        moonlightSonata.setDescription("Piano Sonata No. 14");
        moonlightSonata.setOwner(testOwner1);
        moonlightSonata.setPrice(9.99);
        musicSheetRepository.save(moonlightSonata);

        MusicSheet bohemianRhapsody = new MusicSheet();
        bohemianRhapsody.setName("Bohemian Rhapsody");
        bohemianRhapsody.setComposer("Freddie Mercury");
        bohemianRhapsody.setCategory("ROCK");
        bohemianRhapsody.setDescription("Queen masterpiece");
        // reuse testOwner1 for the second sheet
        bohemianRhapsody.setOwner(testOwner1);
        bohemianRhapsody.setPrice(12.99);
        musicSheetRepository.save(bohemianRhapsody);

        MusicSheet autumnLeaves = new MusicSheet();
        autumnLeaves.setName("Autumn Leaves");
        autumnLeaves.setComposer("Joseph Kosma");
        autumnLeaves.setCategory("JAZZ");
        autumnLeaves.setDescription("Jazz standard");
        com.example.OLSHEETS.data.User owner2 = userRepository.save(new com.example.OLSHEETS.data.User("owner2", "owner2@example.com", "owner2", "123"));
        autumnLeaves.setOwner(owner2);
        autumnLeaves.setPrice(7.99);
        musicSheetRepository.save(autumnLeaves);
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        sheetBookingRepository.deleteAll();
        musicSheetRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetMusicSheetById_WithExistingSheet_ShouldReturnSheet() throws Exception {
        MusicSheet savedSheet = musicSheetRepository.findByNameContainingIgnoreCase("Moonlight Sonata").get(0);
        
        mockMvc.perform(get("/api/sheets/" + savedSheet.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(savedSheet.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Moonlight Sonata")))
                .andExpect(jsonPath("$.composer", is("Beethoven")))
                .andExpect(jsonPath("$.category", is("CLASSICAL")))
                .andExpect(jsonPath("$.description", is("Piano Sonata No. 14")))
                .andExpect(jsonPath("$.price", is(9.99)))
                .andExpect(jsonPath("$.owner.id", notNullValue()));
    }

    @Test
    void testGetMusicSheetById_WithNonExistentSheet_ShouldReturnBadRequest() throws Exception {
        Long nonExistentId = 99999L;
        
        mockMvc.perform(get("/api/sheets/" + nonExistentId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchSheets_WithExactMatch_ShouldReturnOneSheet() throws Exception {
        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "Moonlight Sonata"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Moonlight Sonata")))
                .andExpect(jsonPath("$[0].composer", is("Beethoven")))
                .andExpect(jsonPath("$[0].category", is("CLASSICAL")))
                .andExpect(jsonPath("$[0].price", is(9.99)))
                .andExpect(jsonPath("$[0].owner.id", is(testOwner1.getId().intValue())));
    }

    @Test
    void testSearchSheets_WithPartialMatch_ShouldReturnSheet() throws Exception {
        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "Rhapsody"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Bohemian Rhapsody")));
    }

    @Test
    void testSearchSheets_WithNoMatch_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "Symphony"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testSearchSheets_CaseInsensitive_ShouldReturnSheets() throws Exception {
        // Test lowercase
        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "moonlight"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Test uppercase
        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "MOONLIGHT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Test mixed case
        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "MoOnLiGhT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testSearchSheets_WithPartialWord_ShouldReturnSheets() throws Exception {
        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "Sonata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Moonlight Sonata")));
    }

    @Test
    void testFilterByCategory_WithClassicalCategory_ShouldReturnClassicalSheets() throws Exception {
        mockMvc.perform(get("/api/sheets/filter/category")
                        .param("category", "CLASSICAL"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Moonlight Sonata")))
                .andExpect(jsonPath("$[0].category", is("CLASSICAL")));
    }

    @Test
    void testFilterByCategory_WithRockCategory_ShouldReturnRockSheets() throws Exception {
        mockMvc.perform(get("/api/sheets/filter/category")
                        .param("category", "ROCK"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Bohemian Rhapsody")))
                .andExpect(jsonPath("$[0].category", is("ROCK")));
    }

    @Test
    void testFilterByCategory_WithJazzCategory_ShouldReturnJazzSheets() throws Exception {
        mockMvc.perform(get("/api/sheets/filter/category")
                        .param("category", "JAZZ"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Autumn Leaves")))
                .andExpect(jsonPath("$[0].category", is("JAZZ")));
    }

    @Test
    void testFilterByCategory_WithNonExistentCategory_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/sheets/filter/category")
                        .param("category", "POP"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testSearchSheets_VerifyAllFieldsReturned() throws Exception {
        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "Moonlight"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].name", is("Moonlight Sonata")))
                .andExpect(jsonPath("$[0].composer", is("Beethoven")))
                .andExpect(jsonPath("$[0].category", is("CLASSICAL")))
                .andExpect(jsonPath("$[0].description", is("Piano Sonata No. 14")))
                .andExpect(jsonPath("$[0].owner.id", is(testOwner1.getId().intValue())))
                .andExpect(jsonPath("$[0].price", is(9.99)));
    }

    // Pricing Management Integration Tests

    @Test
    void testUpdatePrice_WithValidPrice_ShouldUpdateAndPersist() throws Exception {
        // Get the saved sheet
        MusicSheet savedSheet = musicSheetRepository.findAll().get(0);
        Long itemId = savedSheet.getId();
        Double originalPrice = savedSheet.getPrice();
        Double newPrice = 15.99;

        // Update the price
        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 15.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId", is(itemId.intValue())))
                .andExpect(jsonPath("$.itemName", is(savedSheet.getName())))
                .andExpect(jsonPath("$.newPrice", is(15.99)));

        // Verify the price was actually updated in the database
        MusicSheet updatedSheet = musicSheetRepository.findById(itemId).orElseThrow();
        assertEquals(newPrice, updatedSheet.getPrice());
        assertNotEquals(originalPrice, updatedSheet.getPrice());
    }

    @Test
    void testUpdatePrice_ForDifferentSheets_ShouldUpdateIndependently() throws Exception {
        MusicSheet sheet1 = musicSheetRepository.findAll().get(0);
        MusicSheet sheet2 = musicSheetRepository.findAll().get(1);

        // Update first sheet
        mockMvc.perform(put("/api/items/price/" + sheet1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 20.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newPrice", is(20.0)));

        // Update second sheet
        mockMvc.perform(put("/api/items/price/" + sheet2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 25.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newPrice", is(25.0)));

        // Verify both updates persisted independently
        MusicSheet updatedSheet1 = musicSheetRepository.findById(sheet1.getId()).orElseThrow();
        MusicSheet updatedSheet2 = musicSheetRepository.findById(sheet2.getId()).orElseThrow();
        assertEquals(20.0, updatedSheet1.getPrice());
        assertEquals(25.0, updatedSheet2.getPrice());
    }

    @Test
    void testUpdatePrice_WithNegativePrice_ShouldReturnBadRequest() throws Exception {
        MusicSheet savedSheet = musicSheetRepository.findAll().get(0);
        Long itemId = savedSheet.getId();
        Double originalPrice = savedSheet.getPrice();

        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": -10.0}"))
                .andExpect(status().isBadRequest());

        // Verify the price was NOT updated
        MusicSheet unchangedSheet = musicSheetRepository.findById(itemId).orElseThrow();
        assertEquals(originalPrice, unchangedSheet.getPrice());
    }

    @Test
    void testUpdatePrice_WithZeroPrice_ShouldSucceed() throws Exception {
        MusicSheet savedSheet = musicSheetRepository.findAll().get(0);
        Long itemId = savedSheet.getId();

        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 0.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newPrice", is(0.0)));

        MusicSheet updatedSheet = musicSheetRepository.findById(itemId).orElseThrow();
        assertEquals(0.0, updatedSheet.getPrice());
    }

    @Test
    void testGetPrice_WithExistingSheet_ShouldReturnCurrentPrice() throws Exception {
        MusicSheet savedSheet = musicSheetRepository.findAll().get(0);
        Long itemId = savedSheet.getId();
        Double expectedPrice = savedSheet.getPrice();

        mockMvc.perform(get("/api/items/price/" + itemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.itemId", is(itemId.intValue())))
                .andExpect(jsonPath("$.price", is(expectedPrice)));
    }

    @Test
    void testGetPrice_AfterUpdate_ShouldReturnNewPrice() throws Exception {
        MusicSheet savedSheet = musicSheetRepository.findAll().get(0);
        Long itemId = savedSheet.getId();

        // Update the price
        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 19.99}"))
                .andExpect(status().isOk());

        // Get the price and verify it was updated
        mockMvc.perform(get("/api/items/price/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(19.99)));
    }

    @Test
    void testGetPrice_WithNonExistentSheet_ShouldReturnNotFound() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(get("/api/items/price/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    // Music Sheet Registration Integration Tests

    @Test
    void testRegisterMusicSheet_WithValidData_ShouldCreateAndPersist() throws Exception {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName("The Four Seasons");
        request.setDescription("Vivaldi's famous concertos");
        request.setPrice(15.99);
        request.setOwnerId(testOwner1.getId());
        request.setCategory("BAROQUE");
        request.setComposer("Antonio Vivaldi");
        request.setInstrumentation("Violin and Orchestra");
        request.setDuration(40.0f);
        request.setPhotoPaths(Arrays.asList("/photos/vivaldi.jpg"));

        long countBefore = musicSheetRepository.count();

        mockMvc.perform(post("/api/sheets/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("The Four Seasons")))
                .andExpect(jsonPath("$.description", is("Vivaldi's famous concertos")))
                .andExpect(jsonPath("$.price", is(15.99)))
                .andExpect(jsonPath("$.category", is("BAROQUE")))
                .andExpect(jsonPath("$.composer", is("Antonio Vivaldi")))
                .andExpect(jsonPath("$.instrumentation", is("Violin and Orchestra")))
                .andExpect(jsonPath("$.duration", is(40.0)));

        // Verify persisted in database
        long countAfter = musicSheetRepository.count();
        assertEquals(countBefore + 1, countAfter);

        // Verify the sheet can be found by search
        List<MusicSheet> sheets = musicSheetRepository.findByNameContainingIgnoreCase("Four Seasons");
        assertEquals(1, sheets.size());
        assertEquals("The Four Seasons", sheets.get(0).getName());
    }

    @Test
    void testRegisterMusicSheet_WithPhotos_ShouldCreateFileReferences() throws Exception {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName("Für Elise");
        request.setDescription("Famous Beethoven composition for piano");
        request.setPrice(7.99);
        request.setOwnerId(testOwner1.getId());
        request.setCategory("CLASSICAL");
        request.setComposer("Ludwig van Beethoven");
        request.setInstrumentation("Piano");
        request.setDuration(3.5f);
        request.setPhotoPaths(Arrays.asList("/photos/sheet1.jpg", "/photos/sheet2.jpg"));

        mockMvc.perform(post("/api/sheets/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileReferences", hasSize(2)));
    }

    @Test
    void testRegisterMusicSheet_WithoutPhotos_ShouldSucceed() throws Exception {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName("Claire de Lune");
        request.setDescription("Debussy piano piece");
        request.setPrice(8.99);
        request.setOwnerId(testOwner1.getId());
        request.setCategory("IMPRESSIONIST");
        request.setComposer("Claude Debussy");
        request.setInstrumentation("Piano");
        request.setDuration(4.5f);

        mockMvc.perform(post("/api/sheets/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Claire de Lune")))
                .andExpect(jsonPath("$.description", is("Debussy piano piece")));
    }

    @Test
    void testRegisterMusicSheet_ThenSearchByName_ShouldFindSheet() throws Exception {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName("Canon in D Major");
        request.setDescription("Pachelbel's Canon");
        request.setPrice(6.99);
        request.setOwnerId(testOwner1.getId());
        request.setCategory("BAROQUE");
        request.setComposer("Johann Pachelbel");
        request.setInstrumentation("String Quartet");
        request.setDuration(5.0f);

        // Register the sheet
        mockMvc.perform(post("/api/sheets/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Search for it
        mockMvc.perform(get("/api/sheets/search")
                        .param("name", "Canon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Canon in D Major")))
                .andExpect(jsonPath("$[0].composer", is("Johann Pachelbel")));
    }

    @Test
    void testRegisterMusicSheet_ThenFilterByCategory_ShouldFindSheet() throws Exception {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName("Blue Rondo à la Turk");
        request.setDescription("Dave Brubeck jazz piece");
        request.setPrice(11.99);
        request.setOwnerId(testOwner1.getId());
        request.setCategory("BEBOP");
        request.setComposer("Dave Brubeck");
        request.setInstrumentation("Piano");
        request.setDuration(6.5f);

        // Register the sheet
        mockMvc.perform(post("/api/sheets/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Filter by category
        mockMvc.perform(get("/api/sheets/filter/category")
                        .param("category", "BEBOP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Blue Rondo à la Turk")));
    }

    @Test
    void testRegisterMultipleMusicSheets_ShouldAllPersist() throws Exception {
        long countBefore = musicSheetRepository.count();

        // Register first sheet
        MusicSheetRegistrationRequest request1 = new MusicSheetRegistrationRequest();
        request1.setName("Gymnopédie No. 1");
        request1.setDescription("Erik Satie piano piece");
        request1.setPrice(5.99);
        request1.setOwnerId(testOwner1.getId());
        request1.setCategory("IMPRESSIONIST");
        request1.setComposer("Erik Satie");
        request1.setInstrumentation("Piano");

        mockMvc.perform(post("/api/sheets/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Register second sheet
        MusicSheetRegistrationRequest request2 = new MusicSheetRegistrationRequest();
        request2.setName("Prelude in C Major");
        request2.setDescription("Bach Well-Tempered Clavier");
        request2.setPrice(6.99);
        request2.setOwnerId(testOwner1.getId());
        request2.setCategory("BAROQUE");
        request2.setComposer("J.S. Bach");
        request2.setInstrumentation("Piano");

        mockMvc.perform(post("/api/sheets/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // Verify both persisted
        long countAfter = musicSheetRepository.count();
        assertEquals(countBefore + 2, countAfter);
    }
}
