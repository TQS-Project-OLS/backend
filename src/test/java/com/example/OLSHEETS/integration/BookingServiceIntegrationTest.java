package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BookingServiceIntegrationTest {

    @Autowired
    BookingService bookingService;

    @Autowired
    BookingRepository bookingRepository;

    @BeforeEach
    void cleanup(){
        bookingRepository.deleteAll();
    }

    @Test
    void shouldCreateBookingWhenNoOverlap(){
        Booking b = bookingService.createBooking(1L, 10L, 100L, LocalDate.of(2025,12,1), LocalDate.of(2025,12,5));
        assertThat(b.getId()).isNotNull();
        assertThat(b.getInstrumentId()).isEqualTo(1L);
        assertThat(b.getOwnerId()).isEqualTo(10L);
        assertThat(b.getRenterId()).isEqualTo(100L);
    }

    @Test
    void shouldRejectOverlappingBooking(){
        bookingService.createBooking(2L, 10L, 100L, LocalDate.of(2025,12,10), LocalDate.of(2025,12,15));

        assertThatThrownBy(() ->
                bookingService.createBooking(2L, 10L, 200L, LocalDate.of(2025,12,14), LocalDate.of(2025,12,20))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldApproveBookingWhenOwnerIsAuthorized() {
        Booking booking = bookingService.createBooking(3L, 10L, 100L, LocalDate.of(2025,12,1), LocalDate.of(2025,12,5));
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

        Booking approved = bookingService.approveBooking(booking.getId(), 10L);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(approved.getId()).isEqualTo(booking.getId());
    }

    @Test
    void shouldRejectBookingWhenOwnerIsAuthorized() {
        Booking booking = bookingService.createBooking(4L, 10L, 100L, LocalDate.of(2025,12,1), LocalDate.of(2025,12,5));
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

        Booking rejected = bookingService.rejectBooking(booking.getId(), 10L);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(rejected.getId()).isEqualTo(booking.getId());
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerTriesToApprove() {
        Booking booking = bookingService.createBooking(5L, 10L, 100L, LocalDate.of(2025,12,1), LocalDate.of(2025,12,5));

        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), 999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not authorized");

        Booking found = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerTriesToReject() {
        Booking booking = bookingService.createBooking(6L, 10L, 100L, LocalDate.of(2025,12,1), LocalDate.of(2025,12,5));

        assertThatThrownBy(() -> bookingService.rejectBooking(booking.getId(), 999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not authorized");

        Booking found = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void shouldThrowExceptionWhenApprovingNonExistentBooking() {
        assertThatThrownBy(() -> bookingService.approveBooking(999L, 10L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Booking not found");
    }

    @Test
    void shouldThrowExceptionWhenRejectingNonExistentBooking() {
        assertThatThrownBy(() -> bookingService.rejectBooking(999L, 10L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Booking not found");
    }

    @Test
    void shouldThrowExceptionWhenApprovingAlreadyApprovedBooking() {
        Booking booking = bookingService.createBooking(7L, 10L, 100L, LocalDate.of(2025,12,1), LocalDate.of(2025,12,5));
        bookingService.approveBooking(booking.getId(), 10L);

        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), 10L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already been approved");
    }

    @Test
    void shouldThrowExceptionWhenRejectingAlreadyRejectedBooking() {
        Booking booking = bookingService.createBooking(8L, 10L, 100L, LocalDate.of(2025,12,1), LocalDate.of(2025,12,5));
        bookingService.rejectBooking(booking.getId(), 10L);

        assertThatThrownBy(() -> bookingService.rejectBooking(booking.getId(), 10L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already been rejected");
    }
}