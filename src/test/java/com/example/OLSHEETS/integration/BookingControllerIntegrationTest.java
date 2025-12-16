package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.repository.AvailabilityRepository;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.repository.ItemRepository;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import com.example.OLSHEETS.repository.PaymentRepository;
import com.example.OLSHEETS.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
    "spring.main.lazy-initialization=true"
})
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SheetBookingRepository sheetBookingRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private com.example.OLSHEETS.repository.UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Instrument instrument;
    private com.example.OLSHEETS.data.User owner;
    private String ownerToken;

    @BeforeEach
    void cleanup() {
        // Clear security context
        SecurityContextHolder.clearContext();
        
        // Delete in correct order to avoid FK constraint errors
        // First delete payments that reference bookings
        paymentRepository.deleteAll();
        // Then delete bookings
        bookingRepository.deleteAll();
        // Then delete sheet bookings
        sheetBookingRepository.deleteAll();
        // Then delete availability
        availabilityRepository.deleteAll();
        // Then delete music sheets
        musicSheetRepository.deleteAll();
        // Then delete items
        itemRepository.deleteAll();
        // Finally delete users
        userRepository.deleteAll();
        
        instrument = new Instrument();
        instrument.setName("Test Guitar");
        instrument.setDescription("A test guitar");
        owner = userRepository.save(new com.example.OLSHEETS.data.User("owner10", "owner10@example.com", "owner10", "123"));
        ownerToken = jwtUtil.generateToken(owner.getUsername());
        instrument.setOwner(owner);
        instrument.setPrice(50.0);
        instrument.setAge(2);
        instrument.setType(InstrumentType.ELECTRIC);
        instrument.setFamily(InstrumentFamily.STRING);
        instrument = itemRepository.save(instrument);
    }
    
    private void setupSecurityContext(String username) {
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testApproveBooking_Success() throws Exception {
        setupSecurityContext(owner.getUsername());
        
        User renter = userRepository.save(new User("renter100", "renter100@test.com", "Renter 100", "password123"));
        Booking booking = new Booking(instrument, renter, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/approve")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("APPROVED")))
            .andExpect(jsonPath("$.renter.id", is(renter.getId().intValue())));
    }

    @Test
    void testApproveBooking_UnauthorizedOwner() throws Exception {
        // Create an unauthorized user (not the owner)
        User unauthorizedUser = userRepository.save(new User("otheruser", "other@example.com", "OtherUser", "password123"));
        String unauthorizedToken = jwtUtil.generateToken(unauthorizedUser.getUsername());
        setupSecurityContext(unauthorizedUser.getUsername());
        
        User renter = userRepository.save(new User("renter100", "renter100@test.com", "Renter 100", "password123"));
        Booking booking = new Booking(instrument, renter, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/approve")
                .header("Authorization", "Bearer " + unauthorizedToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("not authorized")));
    }

    @Test
    void testApproveBooking_AlreadyApproved() throws Exception {
        setupSecurityContext(owner.getUsername());
        
        User renter = userRepository.save(new User("renter100", "renter100@test.com", "Renter 100", "password123"));
        Booking booking = new Booking(instrument, renter, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/approve")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("already been approved")));
    }

    @Test
    void testApproveBooking_NotFound() throws Exception {
        setupSecurityContext(owner.getUsername());
        
        mockMvc.perform(put("/api/bookings/999/approve")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Booking not found")));
    }

    @Test
    void testRejectBooking_Success() throws Exception {
        setupSecurityContext(owner.getUsername());
        
        User renter = userRepository.save(new User("renter100", "renter100@test.com", "Renter 100", "password123"));
        Booking booking = new Booking(instrument, renter, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/reject")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("REJECTED")))
            .andExpect(jsonPath("$.renter.id", is(renter.getId().intValue())));
    }

    @Test
    void testRejectBooking_UnauthorizedOwner() throws Exception {
        // Create an unauthorized user (not the owner)
        User unauthorizedUser = userRepository.save(new User("otheruser", "other@example.com", "OtherUser", "password123"));
        String unauthorizedToken = jwtUtil.generateToken(unauthorizedUser.getUsername());
        setupSecurityContext(unauthorizedUser.getUsername());
        
        User renter = userRepository.save(new User("renter100", "renter100@test.com", "Renter 100", "password123"));
        Booking booking = new Booking(instrument, renter, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/reject")
                .header("Authorization", "Bearer " + unauthorizedToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("not authorized")));
    }

    @Test
    void testRejectBooking_AlreadyRejected() throws Exception {
        setupSecurityContext(owner.getUsername());
        
        User renter = userRepository.save(new User("renter100", "renter100@test.com", "Renter 100", "password123"));
        Booking booking = new Booking(instrument, renter, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/reject")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("already been rejected")));
    }

    @Test
    void testRejectBooking_NotFound() throws Exception {
        setupSecurityContext(owner.getUsername());
        
        mockMvc.perform(put("/api/bookings/999/reject")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Booking not found")));
    }

    @Test
    void testCannotRejectApprovedBooking() throws Exception {
        setupSecurityContext(owner.getUsername());
        
        User renter = userRepository.save(new User("renter100", "renter100@test.com", "Renter 100", "password123"));
        Booking booking = new Booking(instrument, renter, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/reject")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error")
                        .value(org.hamcrest.Matchers.containsString("Cannot reject an approved booking")));
    }

    @Test
    void testCannotApproveRejectedBooking() throws Exception {
        setupSecurityContext(owner.getUsername());
        
        User renter = userRepository.save(new User("renter100", "renter100@test.com", "Renter 100", "password123"));
        Booking booking = new Booking(instrument, renter, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/approve")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error")
                        .value(org.hamcrest.Matchers.containsString("Cannot approve a rejected booking")));
    }
}
