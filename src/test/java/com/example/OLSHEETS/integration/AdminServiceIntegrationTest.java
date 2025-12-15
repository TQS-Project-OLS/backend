package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.repository.ItemRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import com.example.OLSHEETS.repository.PaymentRepository;
import com.example.OLSHEETS.service.AdminService;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
    "spring.main.lazy-initialization=true"
})
class AdminServiceIntegrationTest {

    static PostgreSQLContainer<?> myPostgreSQLContainer =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url",myPostgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username",myPostgreSQLContainer::getUsername);
        registry.add("spring.datasource.password",myPostgreSQLContainer::getPassword);

    }

    @BeforeAll
    static void beforeAll(){
        myPostgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll(){
        myPostgreSQLContainer.stop();
    }

    @Autowired
    AdminService adminService;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    SheetBookingRepository sheetBookingRepository;

    @Autowired
    private com.example.OLSHEETS.repository.UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Instrument instrument1;
    private Instrument instrument2;
    private Booking booking1;
    private Booking booking2;
    private Booking booking3;

    private com.example.OLSHEETS.data.User testOwner10;
    private com.example.OLSHEETS.data.User testOwner20;

    @BeforeEach
    void cleanup() {
        // delete in correct order to avoid FK constraint errors
        // First delete payments that reference bookings
        paymentRepository.deleteAll();
        // Then delete bookings
        bookingRepository.deleteAll();
        // Then delete sheet bookings
        sheetBookingRepository.deleteAll();
        // Then delete items
        itemRepository.deleteAll();
        // Finally delete users
        userRepository.deleteAll();

        instrument1 = new Instrument();
        instrument1.setName("Guitar");
        instrument1.setDescription("Electric Guitar");
        testOwner10 = userRepository.save(new com.example.OLSHEETS.data.User("owner10", "owner10@example.com", "owner10", "123"));
        instrument1.setOwner(testOwner10);
        instrument1.setPrice(50.0);
        instrument1.setAge(2);
        instrument1.setType(InstrumentType.ELECTRIC);
        instrument1.setFamily(InstrumentFamily.STRING);
        instrument1 = itemRepository.save(instrument1);

        instrument2 = new Instrument();
        instrument2.setName("Piano");
        instrument2.setDescription("Digital Piano");
        testOwner20 = userRepository.save(new com.example.OLSHEETS.data.User("owner20", "owner20@example.com", "owner20", "123"));
        instrument2.setOwner(testOwner20);
        instrument2.setPrice(100.0);
        instrument2.setAge(1);
        instrument2.setType(InstrumentType.DIGITAL);
        instrument2.setFamily(InstrumentFamily.KEYBOARD);
        instrument2 = itemRepository.save(instrument2);
    }

    @Test
    void shouldGetAllBookingsFromDatabase() {
        com.example.OLSHEETS.data.User u100 = userRepository.save(new com.example.OLSHEETS.data.User("u100", "u100@example.com", "u100", "123"));
        com.example.OLSHEETS.data.User u200 = userRepository.save(new com.example.OLSHEETS.data.User("u200", "u200@example.com", "u200", "123"));

        booking1 = bookingRepository.save(new Booking(instrument1, u100,
            LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5)));
        booking2 = bookingRepository.save(new Booking(instrument2, u200,
            LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15)));

        List<Booking> bookings = adminService.getAllBookings();

        assertThat(bookings).hasSize(2);
        assertThat(bookings).extracting(b -> b.getRenter().getId()).containsExactlyInAnyOrder(u100.getId(), u200.getId());
    }

    @Test
    void shouldFilterBookingsByStatus() {
        com.example.OLSHEETS.data.User u100 = userRepository.save(new com.example.OLSHEETS.data.User("u100", "u100@example.com", "u100", "123"));
        com.example.OLSHEETS.data.User u200 = userRepository.save(new com.example.OLSHEETS.data.User("u200", "u200@example.com", "u200", "123"));
        com.example.OLSHEETS.data.User u300 = userRepository.save(new com.example.OLSHEETS.data.User("u300", "u300@example.com", "u300", "123"));

        booking1 = new Booking(instrument1, u100, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5));
        booking1.setStatus(BookingStatus.PENDING);
        booking1 = bookingRepository.save(booking1);

        booking2 = new Booking(instrument2, u200, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));
        booking2.setStatus(BookingStatus.APPROVED);
        booking2 = bookingRepository.save(booking2);

        booking3 = new Booking(instrument1, u300, LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25));
        booking3.setStatus(BookingStatus.PENDING);
        booking3 = bookingRepository.save(booking3);

        List<Booking> pendingBookings = adminService.getBookingsByStatus(BookingStatus.PENDING);

        assertThat(pendingBookings).hasSize(2);
        assertThat(pendingBookings).allMatch(b -> b.getStatus() == BookingStatus.PENDING);
    }

    @Test
    void shouldFilterBookingsByRenter() {
        com.example.OLSHEETS.data.User u100 = userRepository.save(new com.example.OLSHEETS.data.User("u100", "u100@example.com", "u100", "123"));
        com.example.OLSHEETS.data.User u200 = userRepository.save(new com.example.OLSHEETS.data.User("u200", "u200@example.com", "u200", "123"));

        booking1 = bookingRepository.save(new Booking(instrument1, u100,
            LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5)));
        booking2 = bookingRepository.save(new Booking(instrument2, u100,
            LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15)));
        booking3 = bookingRepository.save(new Booking(instrument1, u200,
            LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25)));

        List<Booking> renter100Bookings = adminService.getBookingsByRenter(u100.getId());

        assertThat(renter100Bookings).hasSize(2);
        assertThat(renter100Bookings).allMatch(b -> b.getRenter().getId().equals(u100.getId()));
    }

    @Test
    void shouldCancelBookingAsAdmin() {
        com.example.OLSHEETS.data.User u100 = userRepository.save(new com.example.OLSHEETS.data.User("u100", "u100@example.com", "u100", "123"));

        booking1 = bookingRepository.save(new Booking(instrument1, u100,
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
        com.example.OLSHEETS.data.User u100 = userRepository.save(new com.example.OLSHEETS.data.User("u100", "u100@example.com", "u100", "123"));
        com.example.OLSHEETS.data.User u200 = userRepository.save(new com.example.OLSHEETS.data.User("u200", "u200@example.com", "u200", "123"));
        com.example.OLSHEETS.data.User u300 = userRepository.save(new com.example.OLSHEETS.data.User("u300", "u300@example.com", "u300", "123"));

        booking1 = new Booking(instrument1, u100, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5));
        booking1.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking1);

        booking2 = new Booking(instrument2, u200, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));
        booking2.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking2);

        booking3 = new Booking(instrument1, u300, LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25));
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
        com.example.OLSHEETS.data.User u100 = userRepository.save(new com.example.OLSHEETS.data.User("u100", "u100@example.com", "u100", "123"));
        com.example.OLSHEETS.data.User u200 = userRepository.save(new com.example.OLSHEETS.data.User("u200", "u200@example.com", "u200", "123"));

        booking1 = bookingRepository.save(new Booking(instrument1, u100,
            LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5)));
        booking2 = bookingRepository.save(new Booking(instrument2, u100,
            LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15)));
        booking3 = bookingRepository.save(new Booking(instrument1, u200,
            LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25)));

        Long activityRenter100 = adminService.getRenterActivity(u100.getId());
        Long activityRenter200 = adminService.getRenterActivity(u200.getId());

        assertThat(activityRenter100).isEqualTo(2L);
        assertThat(activityRenter200).isEqualTo(1L);
    }

    @Test
    void shouldGetOwnerActivity() {
        com.example.OLSHEETS.data.User u100 = userRepository.save(new com.example.OLSHEETS.data.User("u100", "u100@example.com", "u100", "123"));
        com.example.OLSHEETS.data.User u200 = userRepository.save(new com.example.OLSHEETS.data.User("u200", "u200@example.com", "u200", "123"));
        com.example.OLSHEETS.data.User u300 = userRepository.save(new com.example.OLSHEETS.data.User("u300", "u300@example.com", "u300", "123"));

        booking1 = bookingRepository.save(new Booking(instrument1, u100,
            LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5)));
        booking2 = bookingRepository.save(new Booking(instrument1, u200,
            LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15)));
        booking3 = bookingRepository.save(new Booking(instrument2, u300,
            LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25)));

        Long activityOwner10 = adminService.getOwnerActivity(testOwner10.getId());
        Long activityOwner20 = adminService.getOwnerActivity(testOwner20.getId());

        assertThat(activityOwner10).isEqualTo(2L);
        assertThat(activityOwner20).isEqualTo(1L);
    }

    @Test
    void shouldCalculateRevenueByOwner() {
        com.example.OLSHEETS.data.User u100 = userRepository.save(new com.example.OLSHEETS.data.User("u100", "u100@example.com", "u100", "123"));
        com.example.OLSHEETS.data.User u200 = userRepository.save(new com.example.OLSHEETS.data.User("u200", "u200@example.com", "u200", "123"));

        booking1 = new Booking(instrument1, u100, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5));
        booking1.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking1);

        booking2 = new Booking(instrument1, u200, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));
        booking2.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking2);

        Double revenue = adminService.getRevenueByOwner(testOwner10.getId());

        // 2 bookings: (5-1=4 days * 50) + (15-10=5 days * 50) = 200 + 250 = 450
        assertThat(revenue).isEqualTo(450.0);
    }

    @Test
    void shouldCalculateTotalRevenue() {
        com.example.OLSHEETS.data.User u100 = userRepository.save(new com.example.OLSHEETS.data.User("u100", "u100@example.com", "u100", "123"));
        com.example.OLSHEETS.data.User u200 = userRepository.save(new com.example.OLSHEETS.data.User("u200", "u200@example.com", "u200", "123"));

        booking1 = new Booking(instrument1, u100, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5));
        booking1.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking1);

        booking2 = new Booking(instrument2, u200, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));
        booking2.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking2);

        Double totalRevenue = adminService.getTotalRevenue();

        // (5-1=4 days * 50) + (15-10=5 days * 100) = 200 + 500 = 700
        assertThat(totalRevenue).isEqualTo(700.0);
    }

    @Test
    void shouldNotCountPendingOrRejectedBookingsInRevenue() {
        com.example.OLSHEETS.data.User u100 = userRepository.save(new com.example.OLSHEETS.data.User("u100", "u100@example.com", "u100", "123"));
        com.example.OLSHEETS.data.User u200 = userRepository.save(new com.example.OLSHEETS.data.User("u200", "u200@example.com", "u200", "123"));

        booking1 = new Booking(instrument1, u100, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5));
        booking1.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking1);

        booking2 = new Booking(instrument2, u200, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));
        booking2.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking2);

        Double totalRevenue = adminService.getTotalRevenue();

        assertThat(totalRevenue).isEqualTo(0.0);
    }
}
