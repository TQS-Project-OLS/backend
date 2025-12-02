package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void cleanup() {
        bookingRepository.deleteAll();
    }

    @Test
    void testApproveBooking_Success() throws Exception {
        Booking booking = new Booking(1L, 10L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/approve")
                        .param("ownerId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.ownerId", is(10)))
                .andExpect(jsonPath("$.renterId", is(100)));
    }

    @Test
    void testApproveBooking_UnauthorizedOwner() throws Exception {
        Booking booking = new Booking(1L, 10L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/approve")
                        .param("ownerId", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("not authorized")));
    }

    @Test
    void testApproveBooking_AlreadyApproved() throws Exception {
        Booking booking = new Booking(1L, 10L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/approve")
                        .param("ownerId", "10"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("already been approved")));
    }

    @Test
    void testApproveBooking_NotFound() throws Exception {
        mockMvc.perform(put("/api/bookings/999/approve")
                        .param("ownerId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Booking not found")));
    }

    @Test
    void testRejectBooking_Success() throws Exception {
        Booking booking = new Booking(1L, 10L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/reject")
                        .param("ownerId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.ownerId", is(10)))
                .andExpect(jsonPath("$.renterId", is(100)));
    }

    @Test
    void testRejectBooking_UnauthorizedOwner() throws Exception {
        Booking booking = new Booking(1L, 10L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/reject")
                        .param("ownerId", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("not authorized")));
    }

    @Test
    void testRejectBooking_AlreadyRejected() throws Exception {
        Booking booking = new Booking(1L, 10L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/reject")
                        .param("ownerId", "10"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("already been rejected")));
    }

    @Test
    void testRejectBooking_NotFound() throws Exception {
        mockMvc.perform(put("/api/bookings/999/reject")
                        .param("ownerId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Booking not found")));
    }

    @Test
    void testCannotRejectApprovedBooking() throws Exception {
        Booking booking = new Booking(1L, 10L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/reject")
                        .param("ownerId", "10"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Cannot reject an approved booking")));
    }

    @Test
    void testCannotApproveRejectedBooking() throws Exception {
        Booking booking = new Booking(1L, 10L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/approve")
                        .param("ownerId", "10"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Cannot approve a rejected booking")));
    }
}