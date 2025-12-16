package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.repository.ItemRepository;
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

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private com.example.OLSHEETS.repository.UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking;
    private Item item;
    private com.example.OLSHEETS.data.User testUser;

    @BeforeEach
    void setUp() {
        item = new Instrument();
        item.setId(1L);
        com.example.OLSHEETS.data.User owner10 = new com.example.OLSHEETS.data.User("owner10", "owner10@example.com", "owner10");
        owner10.setId(10L);
        item.setOwner(owner10);
        item.setName("Test Guitar");
        item.setPrice(50.0);

        testUser = new com.example.OLSHEETS.data.User("test", "test@example.com", "test");
        testUser.setId(100L);

        booking = new Booking(item, testUser, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setId(1L);
        booking.setStatus(BookingStatus.PENDING);
    }

    @Test
    void whenApproveBookingByOwner_thenStatusIsApproved() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findOverlapping(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(java.util.Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking approved = bookingService.approveBooking(1L, 10);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void whenApproveBookingByNonOwner_thenThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 999))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not authorized");

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenApproveNonExistentBooking_thenThrowException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.approveBooking(999L, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Booking not found");

        verify(bookingRepository, times(1)).findById(999L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenApproveAlreadyApprovedBooking_thenThrowException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 10))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already been approved");

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenRejectBookingByOwner_thenStatusIsRejected() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking rejected = bookingService.rejectBooking(1L, 10);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void whenRejectBookingByNonOwner_thenThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.rejectBooking(1L, 999))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not authorized");

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenRejectNonExistentBooking_thenThrowException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.rejectBooking(999L, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Booking not found");

        verify(bookingRepository, times(1)).findById(999L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenRejectAlreadyRejectedBooking_thenThrowException() {
        booking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.rejectBooking(1L, 10))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already been rejected");

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenCreateValidBooking_thenSuccess() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findOverlapping(anyLong(), any(), any())).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        Booking created = bookingService.createBooking(1L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.PENDING);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void whenCreateBooking_WithStartDateInPast_thenThrowException() {
        assertThatThrownBy(() -> bookingService.createBooking(1L, 100L, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot book items for past dates");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenCreateBooking_WithEndDateBeforeStartDate_thenThrowException() {
        assertThatThrownBy(() -> bookingService.createBooking(1L, 100L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(3)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("End date must be after start date");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenCreateBooking_WithEndDateEqualToStartDate_thenThrowException() {
        LocalDate sameDate = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> bookingService.createBooking(1L, 100L, sameDate, sameDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("End date must be after start date");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenCreateBooking_WithNullStartDate_thenThrowException() {
        assertThatThrownBy(() -> bookingService.createBooking(1L, 100L, null, LocalDate.now().plusDays(3)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Start date cannot be null");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenCreateBooking_WithNullEndDate_thenThrowException() {
        assertThatThrownBy(() -> bookingService.createBooking(1L, 100L, LocalDate.now().plusDays(1), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("End date cannot be null");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenCreateBooking_RenterIsOwner_thenThrowException() {
        // Setup item with owner ID 100L (same as renter)
        com.example.OLSHEETS.data.User ownerSameAsRenter = new com.example.OLSHEETS.data.User("owner", "owner@example.com", "owner");
        ownerSameAsRenter.setId(100L);
        item.setOwner(ownerSameAsRenter);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> bookingService.createBooking(1L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot book your own items");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenCreateBooking_WithValidDatesAndDifferentOwner_thenSuccess() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(bookingRepository.findOverlapping(anyLong(), any(), any())).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking created = bookingService.createBooking(1L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertThat(created).isNotNull();
        verify(itemRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(100L);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void whenApproveBooking_WithNoOverlappingApprovedBookings_thenSuccess() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findOverlapping(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(java.util.Collections.singletonList(booking)); // Only the booking itself
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking approved = bookingService.approveBooking(1L, 10);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).findOverlapping(anyLong(), any(LocalDate.class), any(LocalDate.class));
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void whenApproveBooking_WithOverlappingApprovedBooking_thenThrowException() {
        // Create an already approved booking that overlaps
        Booking approvedOverlapping = new Booking(item, testUser, 
            LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        approvedOverlapping.setId(2L);
        approvedOverlapping.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findOverlapping(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(java.util.Arrays.asList(booking, approvedOverlapping));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 10))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("conflicts with already approved booking");

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).findOverlapping(anyLong(), any(LocalDate.class), any(LocalDate.class));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenApproveBooking_WithOverlappingPendingBooking_thenSuccess() {
        // Create a pending booking that overlaps (should not block approval)
        Booking pendingOverlapping = new Booking(item, testUser, 
            LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        pendingOverlapping.setId(2L);
        pendingOverlapping.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findOverlapping(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(java.util.Arrays.asList(booking, pendingOverlapping));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking approved = bookingService.approveBooking(1L, 10);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository, times(1)).save(booking);
    }
}
