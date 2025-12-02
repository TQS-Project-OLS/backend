package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.dto.InstrumentRegistrationRequest;
import com.example.OLSHEETS.repository.InstrumentRepository;
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
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void testRegisterInstrument_WithValidData_ShouldCreateInstrument() throws Exception {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Gibson Les Paul");
        request.setDescription("Classic electric guitar in excellent condition");
        request.setPrice(1499.99);
        request.setOwnerId(5);
        request.setAge(3);
        request.setType(InstrumentType.ELECTRIC);
        request.setFamily(InstrumentFamily.GUITAR);
        request.setPhotoPaths(Arrays.asList("/photos/guitar1.jpg", "/photos/guitar2.jpg"));

        mockMvc.perform(post("/api/instruments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Gibson Les Paul")))
                .andExpect(jsonPath("$.description", is("Classic electric guitar in excellent condition")))
                .andExpect(jsonPath("$.price", is(1499.99)))
                .andExpect(jsonPath("$.ownerId", is(5)))
                .andExpect(jsonPath("$.age", is(3)))
                .andExpect(jsonPath("$.type", is("ELECTRIC")))
                .andExpect(jsonPath("$.family", is("GUITAR")));

        // Verify it was saved to the database
        assertEquals(4, instrumentRepository.count());
    }

    @Test
    void testRegisterInstrument_WithPhotos_ShouldCreateFileReferences() throws Exception {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Fender Jazz Bass");
        request.setDescription("Professional bass guitar");
        request.setPrice(999.99);
        request.setOwnerId(3);
        request.setAge(2);
        request.setType(InstrumentType.BASS);
        request.setFamily(InstrumentFamily.GUITAR);
        request.setPhotoPaths(Arrays.asList("/photos/bass1.jpg", "/photos/bass2.jpg", "/photos/bass3.jpg"));

        mockMvc.perform(post("/api/instruments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Fender Jazz Bass")))
                .andExpect(jsonPath("$.fileReferences", hasSize(3)))
                .andExpect(jsonPath("$.fileReferences[0].type", is("photo")))
                .andExpect(jsonPath("$.fileReferences[0].path", is("/photos/bass1.jpg")))
                .andExpect(jsonPath("$.fileReferences[1].path", is("/photos/bass2.jpg")))
                .andExpect(jsonPath("$.fileReferences[2].path", is("/photos/bass3.jpg")));
    }

    @Test
    void testRegisterInstrument_WithoutPhotos_ShouldSucceed() throws Exception {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Roland TD-17");
        request.setDescription("Electronic drum kit");
        request.setPrice(1299.99);
        request.setOwnerId(7);
        request.setAge(1);
        request.setType(InstrumentType.DRUMS);
        request.setFamily(InstrumentFamily.PERCUSSION);
        request.setPhotoPaths(null);

        mockMvc.perform(post("/api/instruments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Roland TD-17")))
                .andExpect(jsonPath("$.description", is("Electronic drum kit")))
                .andExpect(jsonPath("$.type", is("DRUMS")))
                .andExpect(jsonPath("$.family", is("PERCUSSION")));
    }

    @Test
    void testRegisterInstrument_ThenSearchByName_ShouldFindNewInstrument() throws Exception {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Taylor 814ce");
        request.setDescription("Premium acoustic guitar");
        request.setPrice(3299.99);
        request.setOwnerId(4);
        request.setAge(0);
        request.setType(InstrumentType.ACOUSTIC);
        request.setFamily(InstrumentFamily.GUITAR);
        request.setPhotoPaths(Collections.singletonList("/photos/taylor.jpg"));

        // Register the instrument
        mockMvc.perform(post("/api/instruments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Search for it by name
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Taylor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Taylor 814ce")))
                .andExpect(jsonPath("$[0].description", is("Premium acoustic guitar")))
                .andExpect(jsonPath("$[0].price", is(3299.99)))
                .andExpect(jsonPath("$[0].fileReferences", hasSize(1)))
                .andExpect(jsonPath("$[0].fileReferences[0].path", is("/photos/taylor.jpg")));
    }

    @Test
    void testRegisterInstrument_WithSinglePhoto_ShouldCreateOneFileReference() throws Exception {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Korg Minilogue");
        request.setDescription("Analog synthesizer");
        request.setPrice(649.99);
        request.setOwnerId(2);
        request.setAge(1);
        request.setType(InstrumentType.SYNTHESIZER);
        request.setFamily(InstrumentFamily.KEYBOARD);
        request.setPhotoPaths(Collections.singletonList("/photos/synth.jpg"));

        mockMvc.perform(post("/api/instruments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileReferences", hasSize(1)))
                .andExpect(jsonPath("$.fileReferences[0].type", is("photo")))
                .andExpect(jsonPath("$.fileReferences[0].path", is("/photos/synth.jpg")));
    }

    @Test
    void testRegisterInstrument_MultipleInstruments_ShouldAllBeSaved() throws Exception {
        InstrumentRegistrationRequest request1 = new InstrumentRegistrationRequest();
        request1.setName("Violin Stradivarius");
        request1.setDescription("Classical violin");
        request1.setPrice(999999.99);
        request1.setOwnerId(10);
        request1.setAge(200);
        request1.setType(InstrumentType.ACOUSTIC);
        request1.setFamily(InstrumentFamily.STRING);
        request1.setPhotoPaths(Collections.singletonList("/photos/violin.jpg"));

        InstrumentRegistrationRequest request2 = new InstrumentRegistrationRequest();
        request2.setName("Trumpet Yamaha YTR-2330");
        request2.setDescription("Beginner trumpet");
        request2.setPrice(399.99);
        request2.setOwnerId(11);
        request2.setAge(0);
        request2.setType(InstrumentType.WIND);
        request2.setFamily(InstrumentFamily.BRASS);
        request2.setPhotoPaths(null);

        // Register first instrument
        mockMvc.perform(post("/api/instruments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Register second instrument
        mockMvc.perform(post("/api/instruments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // Verify both were saved
        assertEquals(5, instrumentRepository.count());
    }
}
