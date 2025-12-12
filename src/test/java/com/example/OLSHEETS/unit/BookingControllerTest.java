package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.BookingController;
import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.repository.UserRepository;
import com.example.OLSHEETS.service.BookingService;
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
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        com.example.OLSHEETS.data.User owner = new com.example.OLSHEETS.data.User("owner", "owner@example.com", "OwnerTest");
        owner.setId(10L);
        item.setOwner(owner);
        item.setName("Test Guitar");
        item.setPrice(50.0);
        
        User testUser = new User("test", "test@example.com", "Test User", "password123");
        testUser.setId(100L);
        booking = new Booking(item, testUser, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setId(1L);
        booking.setStatus(BookingStatus.PENDING);
    }

    @Test
    @Requirement("OLS-60")
    void testApproveBooking_Success() throws Exception {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingService.approveBooking(1L, 10)).thenReturn(booking);

        mockMvc.perform(put("/api/bookings/1/approve")
                        .param("ownerId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.renter.id", is(100)));

        verify(bookingService, times(1)).approveBooking(1L, 10);
    }

    @Test
    @Requirement("OLS-60")
    void testApproveBooking_NotAuthorized() throws Exception {
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
    @Requirement("OLS-60")
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
    @Requirement("OLS-60")
    void testApproveBooking_NotFound() throws Exception {
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
    @Requirement("OLS-60")
    void testRejectBooking_Success() throws Exception {
        booking.setStatus(BookingStatus.REJECTED);
        when(bookingService.rejectBooking(1L, 10)).thenReturn(booking);

        mockMvc.perform(put("/api/bookings/1/reject")
                        .param("ownerId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.renter.id", is(100)));

        verify(bookingService, times(1)).rejectBooking(1L, 10);
    }

    @Test
    @Requirement("OLS-60")
    void testRejectBooking_NotAuthorized() throws Exception {
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
    @Requirement("OLS-60")
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
    @Requirement("OLS-60")
    void testRejectBooking_NotFound() throws Exception {
        when(bookingService.rejectBooking(999L, 10))
                .thenThrow(new IllegalArgumentException("Booking not found with id: 999"));

        mockMvc.perform(put("/api/bookings/999/reject")
                        .param("ownerId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Booking not found with id: 999")));

        verify(bookingService, times(1)).rejectBooking(999L, 10);
    }

    @Test
    @Requirement("OLS-60")
    void testCreateBooking_Success() throws Exception {
        // Setup authentication context
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        User testUser = new User("testuser", "testuser@example.com", "Test User", "password123");
        testUser.setId(1L);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        
        when(bookingService.createBooking(1L, 1L, startDate, endDate)).thenReturn(booking);

        mockMvc.perform(post("/api/bookings")
                        .param("itemId", "1")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(bookingService, times(1)).createBooking(1L, 1L, startDate, endDate);
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @Requirement("OLS-60")
    void testCreateBooking_WithInvalidDates_ShouldReturnBadRequest() throws Exception {
        // Setup authentication context
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        User testUser = new User("testuser", "testuser@example.com", "Test User", "password123");
        testUser.setId(1L);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        LocalDate startDate = LocalDate.now().plusDays(3);
        LocalDate endDate = LocalDate.now().plusDays(1); // End before start
        
        when(bookingService.createBooking(1L, 1L, startDate, endDate))
                .thenThrow(new IllegalArgumentException("End date must be after start date"));

        mockMvc.perform(post("/api/bookings")
                        .param("itemId", "1")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("End date must be after start date")));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(bookingService, times(1)).createBooking(1L, 1L, startDate, endDate);
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @Requirement("OLS-60")
    void testCreateBooking_WithUserNotFound_ShouldReturnBadRequest() throws Exception {
        // Setup authentication context with a user that doesn't exist in DB
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("nonexistentuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());
        
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        mockMvc.perform(post("/api/bookings")
                        .param("itemId", "1")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("User not found")));

        verify(userRepository, times(1)).findByUsername("nonexistentuser");
        verify(bookingService, never()).createBooking(anyLong(), anyLong(), any(), any());
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @Requirement("OLS-60")
    void testCreateBooking_WithNullAuthentication_ShouldReturnBadRequest() throws Exception {
        // Setup null authentication context
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
        
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        mockMvc.perform(post("/api/bookings")
                        .param("itemId", "1")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).findByUsername(anyString());
        verify(bookingService, never()).createBooking(anyLong(), anyLong(), any(), any());
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @Requirement("OLS-60")
    void testCreateBooking_WithNonExistentItem_ShouldReturnBadRequest() throws Exception {
        // Setup authentication context
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        User testUser = new User("testuser", "testuser@example.com", "Test User", "password123");
        testUser.setId(1L);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        
        when(bookingService.createBooking(999L, 1L, startDate, endDate))
                .thenThrow(new IllegalArgumentException("Item not found with id: 999"));

        mockMvc.perform(post("/api/bookings")
                        .param("itemId", "999")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Item not found with id: 999")));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(bookingService, times(1)).createBooking(999L, 1L, startDate, endDate);
        
        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @Requirement("OLS-60")
    void testListBookings_ShouldReturnAllBookings() throws Exception {
        java.util.List<Booking> bookings = java.util.Arrays.asList(booking);
        
        when(bookingService.listBookings()).thenReturn(bookings);

        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));

        verify(bookingService, times(1)).listBookings();
    }
}

