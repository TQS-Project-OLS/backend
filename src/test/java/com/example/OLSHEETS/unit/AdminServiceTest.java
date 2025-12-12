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
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

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
    private com.example.OLSHEETS.data.User user100;
    private com.example.OLSHEETS.data.User user200;
    private com.example.OLSHEETS.data.User user300;

    @BeforeEach
    void setUp() {
        item1 = new Instrument();
        item1.setId(1L);
        com.example.OLSHEETS.data.User owner10 = new com.example.OLSHEETS.data.User("owner10", "owner10@example.com", "owner10");
        owner10.setId(10L);
        item1.setOwner(owner10);
        item1.setName("Test Guitar");
        item1.setPrice(50.0);

        item2 = new Instrument();
        item2.setId(2L);
        com.example.OLSHEETS.data.User owner20 = new com.example.OLSHEETS.data.User("owner20", "owner20@example.com", "owner20");
        owner20.setId(20L);
        item2.setOwner(owner20);
        item2.setName("Test Piano");
        item2.setPrice(100.0);

        user100 = new com.example.OLSHEETS.data.User("user100", "user100@example.com", "user100");
        user100.setId(100L);
        user200 = new com.example.OLSHEETS.data.User("user200", "user200@example.com", "user200");
        user200.setId(200L);
        user300 = new com.example.OLSHEETS.data.User("user300", "user300@example.com", "user300");
        user300.setId(300L);

        booking1 = new Booking(item1, user100, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking1.setId(1L);
        booking1.setStatus(BookingStatus.PENDING);

        booking2 = new Booking(item2, user200, LocalDate.now().plusDays(5), LocalDate.now().plusDays(7));
        booking2.setId(2L);
        booking2.setStatus(BookingStatus.APPROVED);

        booking3 = new Booking(item1, user300, LocalDate.now().plusDays(10), LocalDate.now().plusDays(12));
        booking3.setId(3L);
        booking3.setStatus(BookingStatus.REJECTED);
    }

    @Test
    @Requirement("OLS-40")
    void testGetAllBookings() {
        when(bookingRepository.findAll()).thenReturn(Arrays.asList(booking1, booking2, booking3));

        List<Booking> bookings = adminService.getAllBookings();

        assertThat(bookings).hasSize(3);
        assertThat(bookings).containsExactly(booking1, booking2, booking3);
        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    @Requirement("OLS-40")
    void testGetBookingsByStatus() {
        when(bookingRepository.findByStatus(BookingStatus.PENDING))
            .thenReturn(Arrays.asList(booking1));

        List<Booking> pendingBookings = adminService.getBookingsByStatus(BookingStatus.PENDING);

        assertThat(pendingBookings).hasSize(1);
        assertThat(pendingBookings.get(0).getStatus()).isEqualTo(BookingStatus.PENDING);
        verify(bookingRepository, times(1)).findByStatus(BookingStatus.PENDING);
    }

    @Test
    @Requirement("OLS-40")
    void testGetBookingsByRenter() {
        when(bookingRepository.findByRenterId(100L)).thenReturn(Arrays.asList(booking1));

        List<Booking> renterBookings = adminService.getBookingsByRenter(100L);

        assertThat(renterBookings).hasSize(1);
        assertThat(renterBookings.get(0).getRenter().getId()).isEqualTo(100L);
        verify(bookingRepository, times(1)).findByRenterId(100L);
    }

    @Test
    @Requirement("OLS-40")
    void testCancelBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking1);

        Booking cancelled = adminService.cancelBooking(1L);

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(booking1);
    }

    @Test
    @Requirement("OLS-40")
    void testGetBookingStatistics() {
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
    @Requirement("OLS-40")
    void testGetRenterActivity() {
        when(bookingRepository.countByRenterId(1L)).thenReturn(5L);

        Long activityCount = adminService.getRenterActivity(1L);

        assertThat(activityCount).isEqualTo(5L);
        verify(bookingRepository, times(1)).countByRenterId(1L);
    }

    @Test
    @Requirement("OLS-40")
    void testGetOwnerActivity() {
        when(itemRepository.findByOwnerId(10L)).thenReturn(Arrays.asList(item1));
        when(bookingRepository.countByItemIn(Arrays.asList(item1))).thenReturn(3L);

        Long activityCount = adminService.getOwnerActivity(10L);

        assertThat(activityCount).isEqualTo(3L);
        verify(itemRepository, times(1)).findByOwnerId(10L);
        verify(bookingRepository, times(1)).countByItemIn(Arrays.asList(item1));
    }

    @Test
    @Requirement("OLS-40")
    void testGetRevenueByOwner() {
        booking2.setStatus(BookingStatus.APPROVED);
        when(itemRepository.findByOwnerId(20L)).thenReturn(Arrays.asList(item2));
        when(bookingRepository.findByItemInAndStatus(Arrays.asList(item2), BookingStatus.APPROVED))
            .thenReturn(Arrays.asList(booking2));

        Double revenue = adminService.getRevenueByOwner(20L);

        assertThat(revenue).isGreaterThan(0);
        verify(bookingRepository, times(1)).findByItemInAndStatus(anyList(), eq(BookingStatus.APPROVED));
    }

    @Test
    @Requirement("OLS-40")
    void testGetTotalRevenue() {
        booking2.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findByStatus(BookingStatus.APPROVED))
            .thenReturn(Arrays.asList(booking2));

        Double totalRevenue = adminService.getTotalRevenue();

        assertThat(totalRevenue).isGreaterThan(0);
        verify(bookingRepository, times(1)).findByStatus(BookingStatus.APPROVED);
    }
}