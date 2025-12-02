package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.repository.InstrumentRepository;
import com.example.OLSHEETS.data.InstrumentFamily;
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
class ProductsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @BeforeEach
    void setUp() {
        instrumentRepository.deleteAll();

        Instrument yamahaPiano = new Instrument();
        yamahaPiano.setName("Yamaha P-125");
        yamahaPiano.setDescription("Digital Piano");
        yamahaPiano.setOwnerId(1);
        yamahaPiano.setPrice(599.99);
        yamahaPiano.setAge(2);
        yamahaPiano.setType(InstrumentType.DIGITAL);
        yamahaPiano.setFamily(InstrumentFamily.KEYBOARD);
        instrumentRepository.save(yamahaPiano);

        Instrument fenderGuitar = new Instrument();
        fenderGuitar.setName("Fender Stratocaster");
        fenderGuitar.setDescription("Electric Guitar");
        fenderGuitar.setOwnerId(1);
        fenderGuitar.setPrice(899.99);
        fenderGuitar.setAge(5);
        fenderGuitar.setType(InstrumentType.ELECTRIC);
        fenderGuitar.setFamily(InstrumentFamily.GUITAR);
        instrumentRepository.save(fenderGuitar);

        Instrument yamahaSax = new Instrument();
        yamahaSax.setName("Yamaha YAS-280");
        yamahaSax.setDescription("Alto Saxophone");
        yamahaSax.setOwnerId(2);
        yamahaSax.setPrice(1299.99);
        yamahaSax.setAge(1);
        yamahaSax.setType(InstrumentType.WIND);
        yamahaSax.setFamily(InstrumentFamily.WOODWIND);
        instrumentRepository.save(yamahaSax);
    }

    @AfterEach
    void tearDown() {
        instrumentRepository.deleteAll();
    }

    @Test
    void testSearchInstruments_WithExactMatch_ShouldReturnOneInstrument() throws Exception {
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Yamaha P-125"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Yamaha P-125")))
                .andExpect(jsonPath("$[0].description", is("Digital Piano")))
                .andExpect(jsonPath("$[0].price", is(599.99)))
                .andExpect(jsonPath("$[0].ownerId", is(1)))
                .andExpect(jsonPath("$[0].age", is(2)))
                .andExpect(jsonPath("$[0].type", is("DIGITAL")))
                .andExpect(jsonPath("$[0].family", is("KEYBOARD")));
    }

    @Test
    void testSearchInstruments_WithPartialMatch_ShouldReturnMultipleInstruments() throws Exception {
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Yamaha"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Yamaha P-125", "Yamaha YAS-280")));
    }

    @Test
    void testSearchInstruments_WithNoMatch_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Gibson"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testSearchInstruments_CaseInsensitive_ShouldReturnInstruments() throws Exception {
        // Test lowercase
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "yamaha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Test uppercase
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "YAMAHA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Test mixed case
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "YaMaHa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testSearchInstruments_WithPartialWord_ShouldReturnInstruments() throws Exception {
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Strat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Fender Stratocaster")));
    }

    @Test
    void testSearchInstruments_VerifyAllFieldsReturned() throws Exception {
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Fender"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].name", is("Fender Stratocaster")))
                .andExpect(jsonPath("$[0].description", is("Electric Guitar")))
                .andExpect(jsonPath("$[0].ownerId", is(1)))
                .andExpect(jsonPath("$[0].price", is(899.99)))
                .andExpect(jsonPath("$[0].age", is(5)))
                .andExpect(jsonPath("$[0].type", is("ELECTRIC")))
                .andExpect(jsonPath("$[0].family", is("GUITAR")));
    }

    @Test
    void testSearchInstruments_MultipleSearches_ShouldReturnConsistentResults() throws Exception {
        // First search
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Yamaha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Second search - should return same results
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Yamaha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testSearchInstruments_DifferentInstruments_ShouldReturnCorrectResults() throws Exception {
        // Search for piano
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "P-125"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].family", is("KEYBOARD")));

        // Search for guitar
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Fender"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].family", is("GUITAR")));

        // Search for sax
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "YAS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].family", is("WOODWIND")));
    }
}
