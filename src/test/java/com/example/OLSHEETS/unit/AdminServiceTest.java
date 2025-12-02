package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.repository.ItemRepository;
import com.example.OLSHEETS.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private AdminService adminService;

    private Booking booking1;
    private Booking booking2;
    private Booking booking3;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        item1 = new Instrument();
        item1.setId(1L);
        item1.setOwnerId(10);
        item1.setName("Test Guitar");
        item1.setPrice(50.0);

        item2 = new Instrument();
        item2.setId(2L);
        item2.setOwnerId(20);
        item2.setName("Test Piano");
        item2.setPrice(100.0);

        booking1 = new Booking(item1, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking1.setId(1L);
        booking1.setStatus(BookingStatus.PENDING);

        booking2 = new Booking(item2, 200L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(7));
        booking2.setId(2L);
        booking2.setStatus(BookingStatus.APPROVED);

        booking3 = new Booking(item1, 300L, LocalDate.now().plusDays(10), LocalDate.now().plusDays(12));
        booking3.setId(3L);
        booking3.setStatus(BookingStatus.REJECTED);
    }

    @Test
    void whenGetAllBookings_thenReturnAllBookings() {
        when(bookingRepository.findAll()).thenReturn(Arrays.asList(booking1, booking2, booking3));

        List<Booking> bookings = adminService.getAllBookings();

        assertThat(bookings).hasSize(3);
        assertThat(bookings).containsExactly(booking1, booking2, booking3);
        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    void whenGetBookingsByStatus_thenReturnFilteredBookings() {
        when(bookingRepository.findByStatus(BookingStatus.PENDING))
            .thenReturn(Arrays.asList(booking1));

        List<Booking> pendingBookings = adminService.getBookingsByStatus(BookingStatus.PENDING);

        assertThat(pendingBookings).hasSize(1);
        assertThat(pendingBookings.get(0).getStatus()).isEqualTo(BookingStatus.PENDING);
        verify(bookingRepository, times(1)).findByStatus(BookingStatus.PENDING);
    }

    @Test
    void whenGetBookingsByRenter_thenReturnRenterBookings() {
        when(bookingRepository.findByRenterId(100L)).thenReturn(Arrays.asList(booking1));

        List<Booking> renterBookings = adminService.getBookingsByRenter(100L);

        assertThat(renterBookings).hasSize(1);
        assertThat(renterBookings.get(0).getRenterId()).isEqualTo(100L);
        verify(bookingRepository, times(1)).findByRenterId(100L);
    }

    @Test
    void whenCancelBookingAsAdmin_thenBookingIsCancelled() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking1);

        Booking cancelled = adminService.cancelBooking(1L);

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(booking1);
    }

    @Test
    void whenCancelNonExistentBooking_thenThrowException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.cancelBooking(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Booking not found");

        verify(bookingRepository, times(1)).findById(999L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void whenGetBookingStatistics_thenReturnCorrectCounts() {
        when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(5L);
        when(bookingRepository.countByStatus(BookingStatus.APPROVED)).thenReturn(10L);
        when(bookingRepository.countByStatus(BookingStatus.REJECTED)).thenReturn(3L);
        when(bookingRepository.countByStatus(BookingStatus.CANCELLED)).thenReturn(2L);
        when(bookingRepository.count()).thenReturn(20L);

        Map<String, Long> stats = adminService.getBookingStatistics();

        assertThat(stats).containsEntry("total", 20L);
        assertThat(stats).containsEntry("pending", 5L);
        assertThat(stats).containsEntry("approved", 10L);
        assertThat(stats).containsEntry("rejected", 3L);
        assertThat(stats).containsEntry("cancelled", 2L);
        verify(bookingRepository, times(1)).count();
        verify(bookingRepository, times(4)).countByStatus(any());
    }

    @Test
    void whenGetRenterActivity_thenReturnBookingCount() {
        when(bookingRepository.countByRenterId(100L)).thenReturn(5L);

        Long activityCount = adminService.getRenterActivity(100L);

        assertThat(activityCount).isEqualTo(5L);
        verify(bookingRepository, times(1)).countByRenterId(100L);
    }

    @Test
    void whenGetOwnerActivity_thenReturnBookingCount() {
        when(itemRepository.findByOwnerId(10)).thenReturn(Arrays.asList(item1));
        when(bookingRepository.countByItemIn(Arrays.asList(item1))).thenReturn(3L);

        Long activityCount = adminService.getOwnerActivity(10);

        assertThat(activityCount).isEqualTo(3L);
        verify(itemRepository, times(1)).findByOwnerId(10);
        verify(bookingRepository, times(1)).countByItemIn(Arrays.asList(item1));
    }

    @Test
    void whenGetRevenueByOwner_thenReturnTotalRevenue() {
        booking2.setStatus(BookingStatus.APPROVED);
        when(itemRepository.findByOwnerId(20)).thenReturn(Arrays.asList(item2));
        when(bookingRepository.findByItemInAndStatus(Arrays.asList(item2), BookingStatus.APPROVED))
            .thenReturn(Arrays.asList(booking2));

        Double revenue = adminService.getRevenueByOwner(20);

        assertThat(revenue).isGreaterThan(0);
        verify(itemRepository, times(1)).findByOwnerId(20);
        verify(bookingRepository, times(1)).findByItemInAndStatus(anyList(), eq(BookingStatus.APPROVED));
    }

    @Test
    void whenGetTotalRevenue_thenReturnSystemWideRevenue() {
        booking2.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findByStatus(BookingStatus.APPROVED))
            .thenReturn(Arrays.asList(booking2));

        Double totalRevenue = adminService.getTotalRevenue();

        assertThat(totalRevenue).isGreaterThan(0);
        verify(bookingRepository, times(1)).findByStatus(BookingStatus.APPROVED);
    }
}