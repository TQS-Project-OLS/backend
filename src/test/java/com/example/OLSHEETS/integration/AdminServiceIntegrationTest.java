package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.repository.ItemRepository;
import com.example.OLSHEETS.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AdminServiceIntegrationTest {

    @Autowired
    AdminService adminService;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ItemRepository itemRepository;

    private Instrument instrument1;
    private Instrument instrument2;
    private Booking booking1;
    private Booking booking2;
    private Booking booking3;

    @BeforeEach
    void cleanup() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();

        instrument1 = new Instrument();
        instrument1.setName("Guitar");
        instrument1.setDescription("Electric Guitar");
        instrument1.setOwnerId(10);
        instrument1.setPrice(50.0);
        instrument1.setAge(2);
        instrument1.setType(InstrumentType.ELECTRIC);
        instrument1.setFamily(InstrumentFamily.STRING);
        instrument1 = itemRepository.save(instrument1);

        instrument2 = new Instrument();
        instrument2.setName("Piano");
        instrument2.setDescription("Digital Piano");
        instrument2.setOwnerId(20);
        instrument2.setPrice(100.0);
        instrument2.setAge(1);
        instrument2.setType(InstrumentType.DIGITAL);
        instrument2.setFamily(InstrumentFamily.KEYBOARD);
        instrument2 = itemRepository.save(instrument2);
    }

    @Test
    void shouldGetAllBookingsFromDatabase() {
        booking1 = bookingRepository.save(new Booking(instrument1, 100L, 
            LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5)));
        booking2 = bookingRepository.save(new Booking(instrument2, 200L, 
            LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15)));

        List<Booking> bookings = adminService.getAllBookings();

        assertThat(bookings).hasSize(2);
        assertThat(bookings).extracting("renterId").containsExactlyInAnyOrder(100L, 200L);
    }

    @Test
    void shouldFilterBookingsByStatus() {
        booking1 = new Booking(instrument1, 100L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5));
        booking1.setStatus(BookingStatus.PENDING);
        booking1 = bookingRepository.save(booking1);

        booking2 = new Booking(instrument2, 200L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));
        booking2.setStatus(BookingStatus.APPROVED);
        booking2 = bookingRepository.save(booking2);

        booking3 = new Booking(instrument1, 300L, LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25));
        booking3.setStatus(BookingStatus.PENDING);
        booking3 = bookingRepository.save(booking3);

        List<Booking> pendingBookings = adminService.getBookingsByStatus(BookingStatus.PENDING);

        assertThat(pendingBookings).hasSize(2);
        assertThat(pendingBookings).allMatch(b -> b.getStatus() == BookingStatus.PENDING);
    }

    @Test
    void shouldFilterBookingsByRenter() {
        booking1 = bookingRepository.save(new Booking(instrument1, 100L, 
            LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5)));
        booking2 = bookingRepository.save(new Booking(instrument2, 100L, 
            LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15)));
        booking3 = bookingRepository.save(new Booking(instrument1, 200L, 
            LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25)));

        List<Booking> renter100Bookings = adminService.getBookingsByRenter(100L);

        assertThat(renter100Bookings).hasSize(2);
        assertThat(renter100Bookings).allMatch(b -> b.getRenterId().equals(100L));
    }

    @Test
    void shouldCancelBookingAsAdmin() {
        booking1 = bookingRepository.save(new Booking(instrument1, 100L, 
            LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5)));

        Booking cancelled = adminService.cancelBooking(booking1.getId());

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        
        Booking fromDb = bookingRepository.findById(booking1.getId()).orElseThrow();
        assertThat(fromDb.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonExistentBooking() {
        assertThatThrownBy(() -> adminService.cancelBooking(9999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Booking not found");
    }

    @Test
    void shouldGetBookingStatistics() {
        booking1 = new Booking(instrument1, 100L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5));
        booking1.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking1);

        booking2 = new Booking(instrument2, 200L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));
        booking2.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking2);

        booking3 = new Booking(instrument1, 300L, LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25));
        booking3.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking3);

        Map<String, Long> stats = adminService.getBookingStatistics();

        assertThat(stats).containsEntry("total", 3L);
        assertThat(stats).containsEntry("pending", 1L);
        assertThat(stats).containsEntry("approved", 1L);
        assertThat(stats).containsEntry("rejected", 1L);
        assertThat(stats).containsEntry("cancelled", 0L);
    }

    @Test
    void shouldGetRenterActivity() {
        booking1 = bookingRepository.save(new Booking(instrument1, 100L, 
            LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5)));
        booking2 = bookingRepository.save(new Booking(instrument2, 100L, 
            LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15)));
        booking3 = bookingRepository.save(new Booking(instrument1, 200L, 
            LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25)));

        Long activityRenter100 = adminService.getRenterActivity(100L);
        Long activityRenter200 = adminService.getRenterActivity(200L);

        assertThat(activityRenter100).isEqualTo(2L);
        assertThat(activityRenter200).isEqualTo(1L);
    }

    @Test
    void shouldGetOwnerActivity() {
        booking1 = bookingRepository.save(new Booking(instrument1, 100L, 
            LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5)));
        booking2 = bookingRepository.save(new Booking(instrument1, 200L, 
            LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15)));
        booking3 = bookingRepository.save(new Booking(instrument2, 300L, 
            LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25)));

        Long activityOwner10 = adminService.getOwnerActivity(10);
        Long activityOwner20 = adminService.getOwnerActivity(20);

        assertThat(activityOwner10).isEqualTo(2L);
        assertThat(activityOwner20).isEqualTo(1L);
    }

    @Test
    void shouldCalculateRevenueByOwner() {
        booking1 = new Booking(instrument1, 100L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5));
        booking1.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking1);

        booking2 = new Booking(instrument1, 200L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));
        booking2.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking2);

        Double revenue = adminService.getRevenueByOwner(10);

        // 2 bookings: (5-1=4 days * 50) + (15-10=5 days * 50) = 200 + 250 = 450
        assertThat(revenue).isEqualTo(450.0);
    }

    @Test
    void shouldCalculateTotalRevenue() {
        booking1 = new Booking(instrument1, 100L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5));
        booking1.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking1);

        booking2 = new Booking(instrument2, 200L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));
        booking2.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking2);

        Double totalRevenue = adminService.getTotalRevenue();

        // (5-1=4 days * 50) + (15-10=5 days * 100) = 200 + 500 = 700
        assertThat(totalRevenue).isEqualTo(700.0);
    }

    @Test
    void shouldNotCountPendingOrRejectedBookingsInRevenue() {
        booking1 = new Booking(instrument1, 100L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5));
        booking1.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking1);

        booking2 = new Booking(instrument2, 200L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));
        booking2.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking2);

        Double totalRevenue = adminService.getTotalRevenue();

        assertThat(totalRevenue).isEqualTo(0.0);
    }
}