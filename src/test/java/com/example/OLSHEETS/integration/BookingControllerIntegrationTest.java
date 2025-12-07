package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.repository.AvailabilityRepository;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.repository.ItemRepository;
import com.example.OLSHEETS.repository.MusicSheetRepository;

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

    @Autowired
    private SheetBookingRepository sheetBookingRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Instrument instrument;

    @BeforeEach
    void cleanup() {
        sheetBookingRepository.deleteAll();
        bookingRepository.deleteAll();
        availabilityRepository.deleteAll();
        musicSheetRepository.deleteAll();
        itemRepository.deleteAll();

        instrument = new Instrument();
        instrument.setName("Test Guitar");
        instrument.setDescription("A test guitar");
        instrument.setOwnerId(10);
        instrument.setPrice(50.0);
        instrument.setAge(2);
        instrument.setType(InstrumentType.ELECTRIC);
        instrument.setFamily(InstrumentFamily.STRING);
        instrument = itemRepository.save(instrument);
    }

    @Test
    void testApproveBooking_Success() throws Exception {
        Booking booking = new Booking(instrument, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/approve")
                .param("ownerId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.renterId", is(100)));
    }

    @Test
    void testApproveBooking_UnauthorizedOwner() throws Exception {
        Booking booking = new Booking(instrument, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/approve")
                .param("ownerId", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("not authorized")));
    }

    @Test
    void testApproveBooking_AlreadyApproved() throws Exception {
        Booking booking = new Booking(instrument, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
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
        Booking booking = new Booking(instrument, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/reject")
                .param("ownerId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.renterId", is(100)));
    }

    @Test
    void testRejectBooking_UnauthorizedOwner() throws Exception {
        Booking booking = new Booking(instrument, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/reject")
                .param("ownerId", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("not authorized")));
    }

    @Test
    void testRejectBooking_AlreadyRejected() throws Exception {
        Booking booking = new Booking(instrument, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
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
        Booking booking = new Booking(instrument, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/reject")
                .param("ownerId", "10"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error")
                        .value(org.hamcrest.Matchers.containsString("Cannot reject an approved booking")));
    }

    @Test
    void testCannotApproveRejectedBooking() throws Exception {
        Booking booking = new Booking(instrument, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setStatus(BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/approve")
                .param("ownerId", "10"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error")
                        .value(org.hamcrest.Matchers.containsString("Cannot approve a rejected booking")));
    }
}