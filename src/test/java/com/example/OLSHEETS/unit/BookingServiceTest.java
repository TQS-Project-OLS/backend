package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = new Booking(1L, 10L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setId(1L);
        booking.setStatus(BookingStatus.PENDING);
    }

    @Test
    void whenApproveBookingByOwner_thenStatusIsApproved() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking approved = bookingService.approveBooking(1L, 10L);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void whenApproveBookingByNonOwner_thenThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not authorized");

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenApproveNonExistentBooking_thenThrowException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.approveBooking(999L, 10L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Booking not found");

        verify(bookingRepository, times(1)).findById(999L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenApproveAlreadyApprovedBooking_thenThrowException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 10L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already been approved");

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenRejectBookingByOwner_thenStatusIsRejected() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking rejected = bookingService.rejectBooking(1L, 10L);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void whenRejectBookingByNonOwner_thenThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.rejectBooking(1L, 999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not authorized");

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenRejectNonExistentBooking_thenThrowException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.rejectBooking(999L, 10L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Booking not found");

        verify(bookingRepository, times(1)).findById(999L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenRejectAlreadyRejectedBooking_thenThrowException() {
        booking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.rejectBooking(1L, 10L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already been rejected");

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenCreateValidBooking_thenSuccess() {
        when(bookingRepository.findOverlapping(anyLong(), any(), any())).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking created = bookingService.createBooking(1L, 10L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.PENDING);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }
}