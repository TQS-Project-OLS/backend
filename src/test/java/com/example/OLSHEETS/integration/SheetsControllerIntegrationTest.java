package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SheetsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    @Autowired
    private SheetBookingRepository sheetBookingRepository;

    @BeforeEach
    void setUp() {
        sheetBookingRepository.deleteAll();
        musicSheetRepository.deleteAll();

        MusicSheet moonlightSonata = new MusicSheet();
        moonlightSonata.setName("Moonlight Sonata");
        moonlightSonata.setComposer("Beethoven");
        moonlightSonata.setCategory("CLASSICAL");
        moonlightSonata.setDescription("Piano Sonata No. 14");
        moonlightSonata.setOwnerId(1);
        moonlightSonata.setPrice(9.99);
        musicSheetRepository.save(moonlightSonata);

        MusicSheet bohemianRhapsody = new MusicSheet();
        bohemianRhapsody.setName("Bohemian Rhapsody");
        bohemianRhapsody.setComposer("Freddie Mercury");
        bohemianRhapsody.setCategory("ROCK");
        bohemianRhapsody.setDescription("Queen masterpiece");
        bohemianRhapsody.setOwnerId(1);
        bohemianRhapsody.setPrice(12.99);
        musicSheetRepository.save(bohemianRhapsody);

        MusicSheet autumnLeaves = new MusicSheet();
        autumnLeaves.setName("Autumn Leaves");
        autumnLeaves.setComposer("Joseph Kosma");
        autumnLeaves.setCategory("JAZZ");
        autumnLeaves.setDescription("Jazz standard");
        autumnLeaves.setOwnerId(2);
        autumnLeaves.setPrice(7.99);
        musicSheetRepository.save(autumnLeaves);
    }

    @AfterEach
    void tearDown() {
        sheetBookingRepository.deleteAll();
        musicSheetRepository.deleteAll();
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
                .andExpect(jsonPath("$[0].ownerId", is(1)));
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
                .andExpect(jsonPath("$[0].ownerId", is(1)))
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
        mockMvc.perform(put("/api/sheets/price/" + itemId)
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
        mockMvc.perform(put("/api/sheets/price/" + sheet1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 20.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newPrice", is(20.0)));

        // Update second sheet
        mockMvc.perform(put("/api/sheets/price/" + sheet2.getId())
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

        mockMvc.perform(put("/api/sheets/price/" + itemId)
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

        mockMvc.perform(put("/api/sheets/price/" + itemId)
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

        mockMvc.perform(get("/api/sheets/price/" + itemId))
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
        mockMvc.perform(put("/api/sheets/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 19.99}"))
                .andExpect(status().isOk());

        // Get the price and verify it was updated
        mockMvc.perform(get("/api/sheets/price/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(19.99)));
    }

    @Test
    void testGetPrice_WithNonExistentSheet_ShouldReturnNotFound() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(get("/api/sheets/price/" + nonExistentId))
                .andExpect(status().isNotFound());
    }
}
