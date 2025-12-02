package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetCategory;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
