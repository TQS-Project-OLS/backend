package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.ProductsController;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.dto.InstrumentRegistrationRequest;
import com.example.OLSHEETS.repository.UserRepository;
import com.example.OLSHEETS.service.ProductsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.OLSHEETS.security.JwtUtil;
import com.example.OLSHEETS.data.User;
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import java.util.Optional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductsController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(ProductsControllerTest.TestConfig.class)
class ProductsControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtUtil jwtUtil() {
            return org.mockito.Mockito.mock(JwtUtil.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductsService productsService;

    @MockitoBean
    private UserRepository userRepository;

    private Instrument instrument1;
    private Instrument instrument2;

    @BeforeEach
    void setUp() {
        instrument1 = new Instrument();
        instrument1.setId(1L);
        instrument1.setName("Yamaha P-125");
        instrument1.setDescription("Digital Piano");
        instrument1.setPrice(599.99);
        com.example.OLSHEETS.data.User owner = new com.example.OLSHEETS.data.User("owner1", "owner1@example.com", "owner1");
        owner.setId(1L);
        instrument1.setOwner(owner);
        instrument1.setAge(2);
        instrument1.setType(InstrumentType.DIGITAL);
        instrument1.setFamily(InstrumentFamily.KEYBOARD);

        instrument2 = new Instrument();
        instrument2.setId(2L);
        instrument2.setName("Yamaha YAS-280");
        instrument2.setDescription("Alto Sax");
        instrument2.setPrice(1299.99);
        instrument2.setOwner(owner);
        instrument2.setAge(1);
        instrument2.setType(InstrumentType.WIND);
        instrument2.setFamily(InstrumentFamily.WOODWIND);
    }

    @Test
    @Requirement("OLS-58")
    void testSearchInstruments_WithMatchingResults_ShouldReturnInstrumentsList() throws Exception {
        // Amock service method
        List<Instrument> instruments = Arrays.asList(instrument1, instrument2);
        when(productsService.searchInstrumentsByName("Yamaha")).thenReturn(instruments);

        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Yamaha"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Yamaha P-125")))
                .andExpect(jsonPath("$[0].price", is(599.99)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Yamaha YAS-280")))
                .andExpect(jsonPath("$[1].price", is(1299.99)));

        verify(productsService, times(1)).searchInstrumentsByName("Yamaha");
    }

    @Test
    @Requirement("OLS-58")
    void testSearchInstruments_WithNoResults_ShouldReturnEmptyList() throws Exception {
        when(productsService.searchInstrumentsByName("Gibson")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Gibson"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productsService, times(1)).searchInstrumentsByName("Gibson");
    }

    @Test
    @Requirement("OLS-58")
    void testSearchInstruments_WithSingleResult_ShouldReturnOneInstrument() throws Exception {
        List<Instrument> instruments = Collections.singletonList(instrument1);
        when(productsService.searchInstrumentsByName("Yamaha P-125")).thenReturn(instruments);

        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Yamaha P-125"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Yamaha P-125")))
                .andExpect(jsonPath("$[0].type", is("DIGITAL")))
                .andExpect(jsonPath("$[0].family", is("KEYBOARD")));

        verify(productsService, times(1)).searchInstrumentsByName("Yamaha P-125");
    }

    @Test
    @Requirement("OLS-58")
    void testSearchInstruments_CaseInsensitive_ShouldWork() throws Exception {
        List<Instrument> instruments = Arrays.asList(instrument1, instrument2);
        when(productsService.searchInstrumentsByName("yamaha")).thenReturn(instruments);

        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "yamaha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(productsService, times(1)).searchInstrumentsByName("yamaha");
    }

    @Test
    @Requirement("OLS-58")
    void testSearchInstruments_WithAllFieldsReturned() throws Exception {
        List<Instrument> instruments = Collections.singletonList(instrument1);
        when(productsService.searchInstrumentsByName("Piano")).thenReturn(instruments);

        mockMvc.perform(get("/api/instruments/search")
                        .param("name", "Piano"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Yamaha P-125")))
                .andExpect(jsonPath("$[0].description", is("Digital Piano")))
                .andExpect(jsonPath("$[0].price", is(599.99)))
                .andExpect(jsonPath("$[0].owner.id", is(1)))
                .andExpect(jsonPath("$[0].age", is(2)))
                .andExpect(jsonPath("$[0].type", is("DIGITAL")))
                .andExpect(jsonPath("$[0].family", is("KEYBOARD")));
    }

    @Test
    @Requirement("OLS-58")
    void testFilterByFamily_WithMatchingResults_ShouldReturnInstruments() throws Exception {
        List<Instrument> instruments = Collections.singletonList(instrument1);
        when(productsService.filterInstrumentsByFamily(InstrumentFamily.KEYBOARD)).thenReturn(instruments);

        mockMvc.perform(get("/api/instruments/filter/family")
                        .param("family", "KEYBOARD"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Yamaha P-125")));

        verify(productsService, times(1)).filterInstrumentsByFamily(InstrumentFamily.KEYBOARD);
    }

    @Test
    @Requirement("OLS-58")
    void testFilterByFamily_WithNoResults_ShouldReturnEmptyList() throws Exception {
        when(productsService.filterInstrumentsByFamily(InstrumentFamily.BRASS)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/instruments/filter/family")
                        .param("family", "BRASS"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productsService, times(1)).filterInstrumentsByFamily(InstrumentFamily.BRASS);
    }

    @Test
    @Requirement("OLS-58")
    void testFilterByType_WithMatchingResults_ShouldReturnInstrumentsList() throws Exception {
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
        when(productsService.filterInstrumentsByType(InstrumentType.ELECTRIC)).thenReturn(electricInstruments);

        mockMvc.perform(get("/api/instruments/filter/type")
                        .param("type", "ELECTRIC"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(3)))
                .andExpect(jsonPath("$[0].name", is("Fender Stratocaster")))
                .andExpect(jsonPath("$[0].type", is("ELECTRIC")))
                .andExpect(jsonPath("$[1].id", is(4)))
                .andExpect(jsonPath("$[1].name", is("Fender Precision Bass")))
                .andExpect(jsonPath("$[1].type", is("ELECTRIC")));

        verify(productsService, times(1)).filterInstrumentsByType(InstrumentType.ELECTRIC);
    }

    @Test
    @Requirement("OLS-58")
    void testFilterByType_WithNoResults_ShouldReturnEmptyList() throws Exception {
        when(productsService.filterInstrumentsByType(InstrumentType.SYNTHESIZER)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/instruments/filter/type")
                        .param("type", "SYNTHESIZER"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productsService, times(1)).filterInstrumentsByType(InstrumentType.SYNTHESIZER);
    }

    @Test
    @Requirement("OLS-58")
    void testFilterByType_WithSingleResult_ShouldReturnOneInstrument() throws Exception {
        instrument1.setType(InstrumentType.ACOUSTIC);
        List<Instrument> instruments = Collections.singletonList(instrument1);
        when(productsService.filterInstrumentsByType(InstrumentType.ACOUSTIC)).thenReturn(instruments);

        mockMvc.perform(get("/api/instruments/filter/type")
                        .param("type", "ACOUSTIC"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Yamaha P-125")))
                .andExpect(jsonPath("$[0].type", is("ACOUSTIC")));

        verify(productsService, times(1)).filterInstrumentsByType(InstrumentType.ACOUSTIC);
    }
    
    @Test
    @Requirement("OLS-58")
    void testRegisterInstrument_WithValidData_ShouldReturnCreatedInstrument() throws Exception {
        // Mock authentication
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        
        // Mock user
        User user = new User();
        user.setId(5L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
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
        savedInstrument.setOwner(ownerUser);
        savedInstrument.setAge(request.getAge());
        savedInstrument.setType(request.getType());
        savedInstrument.setFamily(request.getFamily());

        when(productsService.registerInstrument(any(InstrumentRegistrationRequest.class))).thenReturn(savedInstrument);

        mockMvc.perform(post("/api/instruments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Gibson Les Paul")))
                .andExpect(jsonPath("$.description", is("Classic electric guitar in excellent condition")))
                .andExpect(jsonPath("$.price", is(1499.99)))
                .andExpect(jsonPath("$.owner.id", is(5)))
                .andExpect(jsonPath("$.age", is(3)))
                .andExpect(jsonPath("$.type", is("ELECTRIC")))
                .andExpect(jsonPath("$.family", is("GUITAR")));

        verify(productsService, times(1)).registerInstrument(any(InstrumentRegistrationRequest.class));
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRegisterInstrument_WithPhotos_ShouldAcceptPhotoList() throws Exception {
        // Mock authentication
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        
        // Mock user
        User user = new User();
        user.setId(3L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
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
        savedInstrument.setDescription(request.getDescription());
        savedInstrument.setPrice(request.getPrice());
        com.example.OLSHEETS.data.User ownerUser2 = new com.example.OLSHEETS.data.User("owner" + request.getOwnerId(), "owner" +request.getOwnerId() + "@a.com", "owner" + request.getOwnerId());
        ownerUser2.setId((long) request.getOwnerId());
        savedInstrument.setOwner(ownerUser2);
        savedInstrument.setAge(request.getAge());
        savedInstrument.setType(request.getType());
        savedInstrument.setFamily(request.getFamily());

        when(productsService.registerInstrument(any(InstrumentRegistrationRequest.class))).thenReturn(savedInstrument);

        mockMvc.perform(post("/api/instruments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.name", is("Fender Jazz Bass")))
                .andExpect(jsonPath("$.description", is("Professional bass guitar")));

        verify(productsService, times(1)).registerInstrument(any(InstrumentRegistrationRequest.class));
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRegisterInstrument_WithoutPhotos_ShouldSucceed() throws Exception {
        // Mock authentication
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        
        // Mock user
        User user = new User();
        user.setId(7L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
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
        savedInstrument.setOwner(ownerUser3);
        savedInstrument.setAge(request.getAge());
        savedInstrument.setType(request.getType());
        savedInstrument.setFamily(request.getFamily());

        when(productsService.registerInstrument(any(InstrumentRegistrationRequest.class))).thenReturn(savedInstrument);

        mockMvc.perform(post("/api/instruments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Roland TD-17")))
                .andExpect(jsonPath("$.description", is("Electronic drum kit")));

        verify(productsService, times(1)).registerInstrument(any(InstrumentRegistrationRequest.class));
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRegisterInstrument_WithCompleteData_ShouldReturnAllFields() throws Exception {
        // Mock authentication
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        
        // Mock user
        User user = new User();
        user.setId(4L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName("Taylor 814ce");
        request.setDescription("Premium acoustic guitar");
        request.setPrice(3299.99);
        request.setOwnerId(4L);
        request.setAge(0);
        request.setType(InstrumentType.ACOUSTIC);
        request.setFamily(InstrumentFamily.GUITAR);
        request.setPhotoPaths(Collections.singletonList("/photos/taylor.jpg"));

        Instrument savedInstrument = new Instrument();
        savedInstrument.setId(14L);
        savedInstrument.setName(request.getName());
        savedInstrument.setDescription(request.getDescription());
        savedInstrument.setPrice(request.getPrice());
        com.example.OLSHEETS.data.User ownerUser4 = new com.example.OLSHEETS.data.User("owner" + request.getOwnerId(), "owner" +request.getOwnerId() + "@a.com", "owner" + request.getOwnerId());
        ownerUser4.setId((long) request.getOwnerId());
        savedInstrument.setOwner(ownerUser4);
        savedInstrument.setAge(request.getAge());
        savedInstrument.setType(request.getType());
        savedInstrument.setFamily(request.getFamily());

        when(productsService.registerInstrument(any(InstrumentRegistrationRequest.class))).thenReturn(savedInstrument);

        mockMvc.perform(post("/api/instruments/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(14)))
                .andExpect(jsonPath("$.name", is("Taylor 814ce")))
                .andExpect(jsonPath("$.description", is("Premium acoustic guitar")))
                .andExpect(jsonPath("$.price", is(3299.99)))
                .andExpect(jsonPath("$.owner.id", is(4)))
                .andExpect(jsonPath("$.age", is(0)))
                .andExpect(jsonPath("$.type", is("ACOUSTIC")))
                .andExpect(jsonPath("$.family", is("GUITAR")));

        verify(productsService, times(1)).registerInstrument(any(InstrumentRegistrationRequest.class));
        
        SecurityContextHolder.clearContext();
    }
}
