package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.PricingController;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.repository.UserRepository;
import com.example.OLSHEETS.service.ProductsService;
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

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PricingController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(PricingControllerTest.TestConfig.class)
class PricingControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtUtil jwtUtil() {
            return org.mockito.Mockito.mock(JwtUtil.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductsService productsService;

    @MockitoBean
    private UserRepository userRepository;

    private Instrument instrument;
    private MusicSheet musicSheet;
    private User owner;

    @BeforeEach
    void setUp() {
        // Setup mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getName()).thenReturn("owner1");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Setup owner user
        owner = new User("owner1", "owner1@example.com", "Owner One", "password123");
        owner.setId(1L);
        when(userRepository.findByUsername("owner1")).thenReturn(Optional.of(owner));

        instrument = new Instrument();
        instrument.setId(1L);
        instrument.setName("Yamaha P-125");
        instrument.setDescription("Digital Piano");
        instrument.setPrice(599.99);
        instrument.setOwner(owner);
        instrument.setAge(2);
        instrument.setType(InstrumentType.DIGITAL);
        instrument.setFamily(InstrumentFamily.KEYBOARD);

        musicSheet = new MusicSheet();
        musicSheet.setId(2L);
        musicSheet.setName("Moonlight Sonata");
        musicSheet.setComposer("Beethoven");
        musicSheet.setCategory("CLASSICAL");
        musicSheet.setDescription("Piano Sonata No. 14");
        musicSheet.setPrice(9.99);
        musicSheet.setOwner(owner);
    }

    @Test
    void testUpdatePrice_WithValidPrice_ShouldReturnUpdatedItem() throws Exception {
        Long itemId = 1L;
        Double newPrice = 699.99;
        instrument.setPrice(newPrice);
        
        when(productsService.updateItemPrice(eq(itemId), eq(newPrice), anyLong())).thenReturn(instrument);

        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 699.99}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.itemId", is(1)))
                .andExpect(jsonPath("$.itemName", is("Yamaha P-125")))
                .andExpect(jsonPath("$.newPrice", is(699.99)));

        verify(productsService, times(1)).updateItemPrice(eq(itemId), eq(newPrice), anyLong());
    }

    @Test
    void testUpdatePrice_WithZeroPrice_ShouldSucceed() throws Exception {
        Long itemId = 1L;
        Double newPrice = 0.0;
        instrument.setPrice(newPrice);
        
        when(productsService.updateItemPrice(eq(itemId), eq(newPrice), anyLong())).thenReturn(instrument);

        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 0.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newPrice", is(0.0)));

        verify(productsService, times(1)).updateItemPrice(eq(itemId), eq(newPrice), anyLong());
    }

    @Test
    void testUpdatePrice_WithNegativePrice_ShouldReturnBadRequest() throws Exception {
        Long itemId = 1L;
        Double negativePrice = -10.0;
        
        when(productsService.updateItemPrice(eq(itemId), eq(negativePrice), anyLong()))
            .thenThrow(new IllegalArgumentException("Price must be a positive number"));

        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": -10.0}"))
                .andExpect(status().isBadRequest());

        verify(productsService, times(1)).updateItemPrice(eq(itemId), eq(negativePrice), anyLong());
    }

    @Test
    void testUpdatePrice_WithNonExistentItem_ShouldReturnBadRequest() throws Exception {
        Long itemId = 999L;
        Double newPrice = 100.0;
        
        when(productsService.updateItemPrice(eq(itemId), eq(newPrice), anyLong()))
            .thenThrow(new IllegalArgumentException("Item not found with id: " + itemId));

        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 100.0}"))
                .andExpect(status().isBadRequest());

        verify(productsService, times(1)).updateItemPrice(eq(itemId), eq(newPrice), anyLong());
    }

    @Test
    void testGetPrice_WithExistingItem_ShouldReturnPrice() throws Exception {
        Long itemId = 1L;
        Double price = 599.99;
        
        when(productsService.getItemPrice(itemId)).thenReturn(price);

        mockMvc.perform(get("/api/items/price/" + itemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.itemId", is(1)))
                .andExpect(jsonPath("$.price", is(599.99)));

        verify(productsService, times(1)).getItemPrice(itemId);
    }

    @Test
    void testGetPrice_WithNonExistentItem_ShouldReturnNotFound() throws Exception {
        Long itemId = 999L;
        
        when(productsService.getItemPrice(itemId))
            .thenThrow(new IllegalArgumentException("Item not found with id: " + itemId));

        mockMvc.perform(get("/api/items/price/" + itemId))
                .andExpect(status().isNotFound());

        verify(productsService, times(1)).getItemPrice(itemId);
    }

    // MusicSheet Pricing Tests

    @Test
    void testUpdatePrice_ForMusicSheet_WithValidPrice_ShouldReturnUpdatedItem() throws Exception {
        Long itemId = 2L;
        Double newPrice = 14.99;
        musicSheet.setPrice(newPrice);
        
        when(productsService.updateItemPrice(eq(itemId), eq(newPrice), anyLong())).thenReturn(musicSheet);

        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": 14.99}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.itemId", is(2)))
                .andExpect(jsonPath("$.itemName", is("Moonlight Sonata")))
                .andExpect(jsonPath("$.newPrice", is(14.99)));

        verify(productsService, times(1)).updateItemPrice(eq(itemId), eq(newPrice), anyLong());
    }

    @Test
    void testUpdatePrice_ForMusicSheet_WithNegativePrice_ShouldReturnBadRequest() throws Exception {
        Long itemId = 2L;
        Double negativePrice = -5.0;
        
        when(productsService.updateItemPrice(eq(itemId), eq(negativePrice), anyLong()))
            .thenThrow(new IllegalArgumentException("Price must be a positive number"));

        mockMvc.perform(put("/api/items/price/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPrice\": -5.0}"))
                .andExpect(status().isBadRequest());

        verify(productsService, times(1)).updateItemPrice(eq(itemId), eq(negativePrice), anyLong());
    }

    @Test
    void testGetPrice_ForMusicSheet_WithExistingItem_ShouldReturnPrice() throws Exception {
        Long itemId = 2L;
        Double price = 9.99;
        
        when(productsService.getItemPrice(itemId)).thenReturn(price);

        mockMvc.perform(get("/api/items/price/" + itemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.itemId", is(2)))
                .andExpect(jsonPath("$.price", is(9.99)));

        verify(productsService, times(1)).getItemPrice(itemId);
    }
}
