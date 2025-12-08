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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.repository.UserRepository;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
    "spring.main.lazy-initialization=true"
})
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.transaction.annotation.Transactional
class ProductsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Setup authentication FIRST
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        
        // Clear and create fresh test user - use a unique email to avoid conflicts
        userRepository.deleteAll();
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser" + System.currentTimeMillis() + "@test.com");
        user.setName("Test User");
        user.setPassword("password");
        user = userRepository.save(user);
        // Clear persistence context and reload to ensure visibility
        userRepository.flush();
        // Verify user exists
        var foundUser = userRepository.findByUsername("testuser");
        if (foundUser.isEmpty()) {
            throw new RuntimeException("User not found after save");
        }
        
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
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
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
        // Test case insensitivity with one example
        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "yamaha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // Pricing Management Integration Tests

    @Test
    void testUpdatePrice_WithValidPrice_ShouldUpdateAndPersist() throws Exception {
        // Get the saved instrument
        Instrument savedInstrument = instrumentRepository.findAll().get(0);
        Long itemId = savedInstrument.getId();
        Double originalPrice = savedInstrument.getPrice();
        Double newPrice = 749.99;

        // Update the price
        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 749.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId", is(itemId.intValue())))
                .andExpect(jsonPath("$.itemName", is(savedInstrument.getName())))
                .andExpect(jsonPath("$.newPrice", is(749.99)));

        // Verify the price was actually updated in the database
        Instrument updatedInstrument = instrumentRepository.findById(itemId).orElseThrow();
        assertEquals(newPrice, updatedInstrument.getPrice());
        assertNotEquals(originalPrice, updatedInstrument.getPrice());
    }


    @Test
    void testUpdatePrice_WithZeroPrice_ShouldSucceed() throws Exception {
        Instrument savedInstrument = instrumentRepository.findAll().get(0);
        Long itemId = savedInstrument.getId();

        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 0.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newPrice", is(0.0)));

        Instrument updatedInstrument = instrumentRepository.findById(itemId).orElseThrow();
        assertEquals(0.0, updatedInstrument.getPrice());
    }

    @Test
    void testUpdatePrice_WithNegativePrice_ShouldReturnBadRequest() throws Exception {
        Instrument savedInstrument = instrumentRepository.findAll().get(0);
        Long itemId = savedInstrument.getId();
        Double originalPrice = savedInstrument.getPrice();

        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": -50.0}"))
                .andExpect(status().isBadRequest());

        // Verify the price was NOT updated
        Instrument unchangedInstrument = instrumentRepository.findById(itemId).orElseThrow();
        assertEquals(originalPrice, unchangedInstrument.getPrice());
    }

    @Test
    void testUpdatePrice_WithNonExistentItem_ShouldReturnBadRequest() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(put("/api/items/price/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 100.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPrice_WithExistingItem_ShouldReturnCurrentPrice() throws Exception {
        Instrument savedInstrument = instrumentRepository.findAll().get(0);
        Long itemId = savedInstrument.getId();
        Double expectedPrice = savedInstrument.getPrice();

        mockMvc.perform(get("/api/items/price/" + itemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.itemId", is(itemId.intValue())))
                .andExpect(jsonPath("$.price", is(expectedPrice)));
    }


    @Test
    void testGetPrice_WithNonExistentItem_ShouldReturnNotFound() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(get("/api/items/price/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

}
