package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.AvailabilityController;
import com.example.OLSHEETS.data.Availability;
import com.example.OLSHEETS.data.AvailabilityReason;
import com.example.OLSHEETS.service.AvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AvailabilityController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(AvailabilityControllerTest.TestConfig.class)
class AvailabilityControllerTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public com.example.OLSHEETS.security.JwtUtil jwtUtil() {
            return org.mockito.Mockito.mock(com.example.OLSHEETS.security.JwtUtil.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvailabilityService availabilityService;

    private Availability availability;

    @BeforeEach
    void setUp() {
        availability = new Availability();
        availability.setId(1L);
        availability.setInstrumentId(1L);
        availability.setStartDate(LocalDate.now().plusDays(1));
        availability.setEndDate(LocalDate.now().plusDays(3));
        availability.setReason(AvailabilityReason.MAINTENANCE);
    }

    @Test
    void testCreateUnavailability() throws Exception {
        when(availabilityService.createUnavailability(anyLong(), any(LocalDate.class), any(LocalDate.class), any(AvailabilityReason.class)))
                .thenReturn(availability);

        mockMvc.perform(post("/api/availability")
                        .param("instrumentId", "1")
                        .param("startDate", LocalDate.now().plusDays(1).toString())
                        .param("endDate", LocalDate.now().plusDays(3).toString())
                        .param("reason", "MAINTENANCE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.instrumentId").value(1));
    }

    @Test
    void testListAvailabilities() throws Exception {
        List<Availability> availabilities = Arrays.asList(availability);
        when(availabilityService.listAvailabilities()).thenReturn(availabilities);

        mockMvc.perform(get("/api/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void testGetInstrumentAvailabilities() throws Exception {
        List<Availability> availabilities = Arrays.asList(availability);
        when(availabilityService.getInstrumentAvailabilities(1L)).thenReturn(availabilities);

        mockMvc.perform(get("/api/availability/instrument/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].instrumentId").value(1));
    }

    @Test
    void testCheckAvailability_Available() throws Exception {
        when(availabilityService.isAvailable(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)))
                .thenReturn(true);

        mockMvc.perform(get("/api/availability/check")
                        .param("instrumentId", "1")
                        .param("startDate", LocalDate.now().plusDays(1).toString())
                        .param("endDate", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void testCheckAvailability_NotAvailable() throws Exception {
        when(availabilityService.isAvailable(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)))
                .thenReturn(false);

        mockMvc.perform(get("/api/availability/check")
                        .param("instrumentId", "1")
                        .param("startDate", LocalDate.now().plusDays(1).toString())
                        .param("endDate", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    void testDeleteUnavailability() throws Exception {
        mockMvc.perform(delete("/api/availability/1"))
                .andExpect(status().isNoContent());
    }
}

