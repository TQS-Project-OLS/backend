package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.BookingController;
import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.repository.UserRepository;
import com.example.OLSHEETS.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.OLSHEETS.security.JwtUtil;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(BookingControllerTest.TestConfig.class)
class BookingControllerTest {

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
    private BookingService bookingService;

    @MockitoBean
    private UserRepository userRepository;

    private Booking booking;
    private Item item;

    @BeforeEach
    void setUp() {
        item = new Instrument();
        item.setId(1L);
        item.setOwnerId(10);
        item.setName("Test Guitar");
        item.setPrice(50.0);
        
        booking = new Booking(item, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setId(1L);
        booking.setStatus(BookingStatus.PENDING);
    }

    @Test
    void testApproveBooking_WithValidOwner_ShouldReturnApprovedBooking() throws Exception {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingService.approveBooking(1L, 10)).thenReturn(booking);

        mockMvc.perform(put("/api/bookings/1/approve")
                        .param("ownerId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.renterId", is(100)));

        verify(bookingService, times(1)).approveBooking(1L, 10);
    }

    @Test
    void testApproveBooking_WithUnauthorizedOwner_ShouldReturnBadRequest() throws Exception {
        when(bookingService.approveBooking(1L, 999))
                .thenThrow(new IllegalArgumentException("You are not authorized to approve this booking"));

        mockMvc.perform(put("/api/bookings/1/approve")
                        .param("ownerId", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("You are not authorized to approve this booking")));

        verify(bookingService, times(1)).approveBooking(1L, 999);
    }

    @Test
    void testApproveBooking_WhenAlreadyApproved_ShouldReturnConflict() throws Exception {
        when(bookingService.approveBooking(1L, 10))
                .thenThrow(new IllegalStateException("Booking has already been approved"));

        mockMvc.perform(put("/api/bookings/1/approve")
                        .param("ownerId", "10"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Booking has already been approved")));

        verify(bookingService, times(1)).approveBooking(1L, 10);
    }

    @Test
    void testApproveBooking_WhenBookingNotFound_ShouldReturnBadRequest() throws Exception {
        when(bookingService.approveBooking(999L, 10))
                .thenThrow(new IllegalArgumentException("Booking not found with id: 999"));

        mockMvc.perform(put("/api/bookings/999/approve")
                        .param("ownerId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Booking not found with id: 999")));

        verify(bookingService, times(1)).approveBooking(999L, 10);
    }

    @Test
    void testRejectBooking_WithValidOwner_ShouldReturnRejectedBooking() throws Exception {
        booking.setStatus(BookingStatus.REJECTED);
        when(bookingService.rejectBooking(1L, 10)).thenReturn(booking);

        mockMvc.perform(put("/api/bookings/1/reject")
                        .param("ownerId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.renterId", is(100)));

        verify(bookingService, times(1)).rejectBooking(1L, 10);
    }

    @Test
    void testRejectBooking_WithUnauthorizedOwner_ShouldReturnBadRequest() throws Exception {
        when(bookingService.rejectBooking(1L, 999))
                .thenThrow(new IllegalArgumentException("You are not authorized to reject this booking"));

        mockMvc.perform(put("/api/bookings/1/reject")
                        .param("ownerId", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("You are not authorized to reject this booking")));

        verify(bookingService, times(1)).rejectBooking(1L, 999);
    }

    @Test
    void testRejectBooking_WhenAlreadyRejected_ShouldReturnConflict() throws Exception {
        when(bookingService.rejectBooking(1L, 10))
                .thenThrow(new IllegalStateException("Booking has already been rejected"));

        mockMvc.perform(put("/api/bookings/1/reject")
                        .param("ownerId", "10"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Booking has already been rejected")));

        verify(bookingService, times(1)).rejectBooking(1L, 10);
    }

    @Test
    void testRejectBooking_WhenBookingNotFound_ShouldReturnBadRequest() throws Exception {
        when(bookingService.rejectBooking(999L, 10))
                .thenThrow(new IllegalArgumentException("Booking not found with id: 999"));

        mockMvc.perform(put("/api/bookings/999/reject")
                        .param("ownerId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Booking not found with id: 999")));

        verify(bookingService, times(1)).rejectBooking(999L, 10);
    }
}