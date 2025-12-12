package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.SheetBookingController;
import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetBooking;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.service.SheetBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.OLSHEETS.security.JwtUtil;
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SheetBookingController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(SheetBookingControllerTest.TestConfig.class)
class SheetBookingControllerTest {

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
    private SheetBookingService bookingService;

    private MusicSheet sheet;
    private SheetBooking booking;

    @BeforeEach
    void setUp() {
        User owner = new User("owner1", "owner1@example.com", "Owner One", "password123");
        owner.setId(1L);
        sheet = new MusicSheet();
        sheet.setTitle("Moonlight Sonata");
        sheet.setCategory("classical");
        sheet.setComposer("Beautiful piece");
        sheet.setPrice(5.00);
        sheet.setOwner(owner);
        sheet.setId(1L);
        com.example.OLSHEETS.data.User testUser = new com.example.OLSHEETS.data.User("tester", "tester@example.com", "tester");
        testUser.setId(100L);
        booking = new SheetBooking(sheet, testUser, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setId(1L);
    }

    @Test
    @Requirement("OLS-60")
    void whenCreateBooking_thenReturn201() throws Exception {
        when(bookingService.createBooking(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(booking);

        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate = LocalDate.now().plusDays(3).toString();

        mockMvc.perform(post("/api/sheets/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sheetId\":1,\"renterId\":100,\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate + "\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.renter.id").value(100));
    }

    @Test
    @Requirement("OLS-60")
    void whenGetBookingsByRenter_thenReturn200() throws Exception {
        when(bookingService.getBookingsByRenter(100L)).thenReturn(List.of(booking));

        mockMvc.perform(get("/api/sheets/bookings/renter/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].renter.id").value(100));
    }

    @Test
    @Requirement("OLS-60")
    void whenGetBookingById_thenReturn200() throws Exception {
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(booking));

        mockMvc.perform(get("/api/sheets/bookings/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void whenGetNonExistentBooking_thenReturn404() throws Exception {
        when(bookingService.getBookingById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sheets/bookings/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @Requirement("OLS-60")
    void whenCreateBookingWithInvalidData_thenReturn400() throws Exception {
        when(bookingService.createBooking(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenThrow(new IllegalArgumentException("Start date must be before end date"));

        String startDate = LocalDate.now().plusDays(5).toString();
        String endDate = LocalDate.now().plusDays(2).toString();

        mockMvc.perform(post("/api/sheets/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sheetId\":1,\"renterId\":100,\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate + "\"}"))
            .andExpect(status().isBadRequest());
    }
}


