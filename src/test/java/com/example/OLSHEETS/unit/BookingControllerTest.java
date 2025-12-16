package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.BookingController;
import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.repository.UserRepository;
import com.example.OLSHEETS.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.OLSHEETS.security.JwtUtil;

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
        // Mock authentication
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("owner");
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        item = new Instrument();
        item.setId(1L);
        com.example.OLSHEETS.data.User owner = new com.example.OLSHEETS.data.User("owner", "owner@example.com", "OwnerTest");
        owner.setId(10L);
        item.setOwner(owner);
        item.setName("Test Guitar");
        item.setPrice(50.0);

        // Mock userRepository to return the owner
        when(userRepository.findByUsername("owner")).thenReturn(java.util.Optional.of(owner));
        
        User testUser = new User("test", "test@example.com", "Test User", "password123");
        testUser.setId(100L);
        booking = new Booking(item, testUser, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setId(1L);
        booking.setStatus(BookingStatus.PENDING);
    }

    @Test
    void testApproveBooking_WithValidOwner_ShouldReturnApprovedBooking() throws Exception {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingService.approveBooking(1L, 10)).thenReturn(booking);

        mockMvc.perform(put("/api/bookings/1/approve"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.renter.id", is(100)));

        verify(bookingService, times(1)).approveBooking(1L, 10);
    }

    @Test
    void testApproveBooking_WithUnauthorizedOwner_ShouldReturnBadRequest() throws Exception {
        // Mock authentication with a different user (not the owner)
        com.example.OLSHEETS.data.User unauthorizedUser = new com.example.OLSHEETS.data.User("otheruser", "other@example.com", "OtherUser");
        unauthorizedUser.setId(999L);
        
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("otheruser");
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("otheruser")).thenReturn(java.util.Optional.of(unauthorizedUser));
        
        when(bookingService.approveBooking(1L, 999))
                .thenThrow(new IllegalArgumentException("You are not authorized to approve this booking"));

        mockMvc.perform(put("/api/bookings/1/approve"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("You are not authorized to approve this booking")));

        verify(bookingService, times(1)).approveBooking(1L, 999);
    }

    @Test
    void testApproveBooking_WhenAlreadyApproved_ShouldReturnConflict() throws Exception {
        when(bookingService.approveBooking(1L, 10))
                .thenThrow(new IllegalStateException("Booking has already been approved"));

        mockMvc.perform(put("/api/bookings/1/approve"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Booking has already been approved")));

        verify(bookingService, times(1)).approveBooking(1L, 10);
    }

    @Test
    void testApproveBooking_WhenBookingNotFound_ShouldReturnBadRequest() throws Exception {
        when(bookingService.approveBooking(999L, 10))
                .thenThrow(new IllegalArgumentException("Booking not found with id: 999"));

        mockMvc.perform(put("/api/bookings/999/approve"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Booking not found with id: 999")));

        verify(bookingService, times(1)).approveBooking(999L, 10);
    }

    @Test
    void testRejectBooking_WithValidOwner_ShouldReturnRejectedBooking() throws Exception {
        booking.setStatus(BookingStatus.REJECTED);
        when(bookingService.rejectBooking(1L, 10)).thenReturn(booking);

        mockMvc.perform(put("/api/bookings/1/reject"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.renter.id", is(100)));

        verify(bookingService, times(1)).rejectBooking(1L, 10);
    }

    @Test
    void testRejectBooking_WithUnauthorizedOwner_ShouldReturnBadRequest() throws Exception {
        // Mock authentication with a different user (not the owner)
        com.example.OLSHEETS.data.User unauthorizedUser = new com.example.OLSHEETS.data.User("otheruser", "other@example.com", "OtherUser");
        unauthorizedUser.setId(999L);
        
        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("otheruser");
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("otheruser")).thenReturn(java.util.Optional.of(unauthorizedUser));
        
        when(bookingService.rejectBooking(1L, 999))
                .thenThrow(new IllegalArgumentException("You are not authorized to reject this booking"));

        mockMvc.perform(put("/api/bookings/1/reject"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("You are not authorized to reject this booking")));

        verify(bookingService, times(1)).rejectBooking(1L, 999);
    }

    @Test
    void testRejectBooking_WhenAlreadyRejected_ShouldReturnConflict() throws Exception {
        when(bookingService.rejectBooking(1L, 10))
                .thenThrow(new IllegalStateException("Booking has already been rejected"));

        mockMvc.perform(put("/api/bookings/1/reject"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Booking has already been rejected")));

        verify(bookingService, times(1)).rejectBooking(1L, 10);
    }

    @Test
    void testRejectBooking_WhenBookingNotFound_ShouldReturnBadRequest() throws Exception {
        when(bookingService.rejectBooking(999L, 10))
                .thenThrow(new IllegalArgumentException("Booking not found with id: 999"));

        mockMvc.perform(put("/api/bookings/999/reject"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Booking not found with id: 999")));

        verify(bookingService, times(1)).rejectBooking(999L, 10);
    }

    @Test
    void testCreateBooking_WithValidData_ShouldReturnCreatedBooking() throws Exception {
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

    @Test
    void testGetMyBookings_WithValidUser_ShouldReturnBookings() throws Exception {
        // Setup authentication context
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        User testUser = new User("testuser", "testuser@example.com", "Test User", "password123");
        testUser.setId(100L);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        java.util.List<Booking> bookings = java.util.Arrays.asList(booking);
        when(bookingService.getBookingsByRenterId(100L)).thenReturn(bookings);

        mockMvc.perform(get("/api/bookings/my-bookings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(bookingService, times(1)).getBookingsByRenterId(100L);
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetMyBookings_WithUserNotFound_ShouldReturnUnauthorized() throws Exception {
        // Setup authentication context with user not in DB
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("nonexistent");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bookings/my-bookings"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("User not found")));

        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(bookingService, never()).getBookingsByRenterId(anyLong());
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetMyBookings_WithServiceException_ShouldReturnBadRequest() throws Exception {
        // Setup authentication context
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        User testUser = new User("testuser", "testuser@example.com", "Test User", "password123");
        testUser.setId(100L);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bookingService.getBookingsByRenterId(100L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/bookings/my-bookings"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Database error")));

        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetMyInstrumentBookings_WithValidUser_ShouldReturnBookings() throws Exception {
        // Setup authentication context
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("owner");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        User ownerUser = new User("owner", "owner@example.com", "OwnerTest");
        ownerUser.setId(10L);
        
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
        
        java.util.List<Booking> bookings = java.util.Arrays.asList(booking);
        when(bookingService.getBookingsByOwnerId(10L)).thenReturn(bookings);

        mockMvc.perform(get("/api/bookings/my-instrument-bookings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(userRepository, times(1)).findByUsername("owner");
        verify(bookingService, times(1)).getBookingsByOwnerId(10L);
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetMyInstrumentBookings_WithUserNotFound_ShouldReturnUnauthorized() throws Exception {
        // Setup authentication context with user not in DB
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("nonexistent");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bookings/my-instrument-bookings"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("User not found")));

        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(bookingService, never()).getBookingsByOwnerId(anyLong());
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetMyInstrumentBookings_WithServiceException_ShouldReturnBadRequest() throws Exception {
        // Setup authentication context
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("owner");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        User ownerUser = new User("owner", "owner@example.com", "OwnerTest");
        ownerUser.setId(10L);
        
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(ownerUser));
        when(bookingService.getBookingsByOwnerId(10L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/bookings/my-instrument-bookings"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("Database error")));

        SecurityContextHolder.clearContext();
    }

    @Test
    void testApproveBooking_WithUserNotFound_ShouldReturnUnauthorized() throws Exception {
        // Setup authentication context with user not in DB
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("nonexistent");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/bookings/1/approve"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("User not found")));

        verify(bookingService, never()).approveBooking(anyLong(), anyInt());
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRejectBooking_WithUserNotFound_ShouldReturnUnauthorized() throws Exception {
        // Setup authentication context with user not in DB
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("nonexistent");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/bookings/1/reject"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error", is("User not found")));

        verify(bookingService, never()).rejectBooking(anyLong(), anyInt());
        
        SecurityContextHolder.clearContext();
    }
}