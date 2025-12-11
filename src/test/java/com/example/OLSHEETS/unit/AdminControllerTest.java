package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.AdminController;
import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.service.AdminService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(AdminControllerTest.TestConfig.class)
class AdminControllerTest {

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
    private AdminService adminService;

    private Booking booking1;
    private Booking booking2;
    private Item item1;

    @BeforeEach
    void setUp() {
        item1 = new Instrument();
        item1.setId(1L);
        com.example.OLSHEETS.data.User owner = new com.example.OLSHEETS.data.User("owner", "owner@example.com", "owner");
        owner.setId(10L);
        item1.setOwner(owner);
        item1.setName("Test Guitar");
        item1.setPrice(50.0);

        User user1 = new User("tester1", "tester1@example.com", "Test User One", "password123");
        user1.setId(100L);
        booking1 = new Booking(item1, user1, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking1.setId(1L);
        booking1.setStatus(BookingStatus.PENDING);

        User user2 = new User("tester2", "tester2@example.com", "Test User Two", "password123");
        user2.setId(200L);
        booking2 = new Booking(item1, user2, LocalDate.now().plusDays(5), LocalDate.now().plusDays(7));
        booking2.setId(2L);
        booking2.setStatus(BookingStatus.APPROVED);
    }

    @Test
    void testGetAllBookings_ShouldReturnAllBookings() throws Exception {
        when(adminService.getAllBookings()).thenReturn(Arrays.asList(booking1, booking2));

        mockMvc.perform(get("/api/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(adminService, times(1)).getAllBookings();
    }

    @Test
    void testGetBookingsByStatus_ShouldReturnFilteredBookings() throws Exception {
        when(adminService.getBookingsByStatus(BookingStatus.PENDING))
                .thenReturn(Arrays.asList(booking1));

        mockMvc.perform(get("/api/admin/bookings/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));

        verify(adminService, times(1)).getBookingsByStatus(BookingStatus.PENDING);
    }

    @Test
    void testGetBookingsByRenter_ShouldReturnRenterBookings() throws Exception {
        when(adminService.getBookingsByRenter(100L)).thenReturn(Arrays.asList(booking1));

        mockMvc.perform(get("/api/admin/bookings/renter/100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].renter.id", is(100)));

        verify(adminService, times(1)).getBookingsByRenter(100L);
    }

    @Test
    void testCancelBooking_ShouldReturnCancelledBooking() throws Exception {
        booking1.setStatus(BookingStatus.CANCELLED);
        when(adminService.cancelBooking(1L)).thenReturn(booking1);

        mockMvc.perform(put("/api/admin/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        verify(adminService, times(1)).cancelBooking(1L);
    }

    @Test
    void testCancelBooking_WhenNotFound_ShouldReturnBadRequest() throws Exception {
        when(adminService.cancelBooking(999L))
                .thenThrow(new IllegalArgumentException("Booking not found with id: 999"));

        mockMvc.perform(put("/api/admin/bookings/999/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Booking not found with id: 999")));

        verify(adminService, times(1)).cancelBooking(999L);
    }

    @Test
    void testGetBookingStatistics_ShouldReturnStats() throws Exception {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", 20L);
        stats.put("pending", 5L);
        stats.put("approved", 10L);
        stats.put("rejected", 3L);
        stats.put("cancelled", 2L);

        when(adminService.getBookingStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/statistics/bookings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.total", is(20)))
                .andExpect(jsonPath("$.pending", is(5)))
                .andExpect(jsonPath("$.approved", is(10)))
                .andExpect(jsonPath("$.rejected", is(3)))
                .andExpect(jsonPath("$.cancelled", is(2)));

        verify(adminService, times(1)).getBookingStatistics();
    }

    @Test
    void testGetRenterActivity_ShouldReturnActivityCount() throws Exception {
        when(adminService.getRenterActivity(100L)).thenReturn(5L);

        mockMvc.perform(get("/api/admin/activity/renter/100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.renterId", is(100)))
                .andExpect(jsonPath("$.bookingCount", is(5)));

        verify(adminService, times(1)).getRenterActivity(100L);
    }

    @Test
    void testGetOwnerActivity_ShouldReturnActivityCount() throws Exception {
        when(adminService.getOwnerActivity(10L)).thenReturn(3L);

        mockMvc.perform(get("/api/admin/activity/owner/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.ownerId", is(10)))
                .andExpect(jsonPath("$.bookingCount", is(3)));

        verify(adminService, times(1)).getOwnerActivity(10L);
    }

    @Test
    void testGetRevenueByOwner_ShouldReturnRevenue() throws Exception {
        when(adminService.getRevenueByOwner(10L)).thenReturn(500.0);

        mockMvc.perform(get("/api/admin/revenue/owner/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.ownerId", is(10)))
                .andExpect(jsonPath("$.revenue", is(500.0)));

        verify(adminService, times(1)).getRevenueByOwner(10L);
    }

    @Test
    void testGetTotalRevenue_ShouldReturnSystemRevenue() throws Exception {
        when(adminService.getTotalRevenue()).thenReturn(2500.0);

        mockMvc.perform(get("/api/admin/revenue/total"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalRevenue", is(2500.0)));

        verify(adminService, times(1)).getTotalRevenue();
    }
}