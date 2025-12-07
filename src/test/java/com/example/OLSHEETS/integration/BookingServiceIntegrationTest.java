package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.repository.ItemRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
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

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    SheetBookingRepository sheetBookingRepository;

        @Autowired
        private com.example.OLSHEETS.repository.UserRepository userRepository;

    private Instrument instrument1;
    private Instrument instrument2;
        private com.example.OLSHEETS.data.User owner10;
        private com.example.OLSHEETS.data.User owner20;
        private com.example.OLSHEETS.data.User renter100;
        private com.example.OLSHEETS.data.User renter200;

    @BeforeEach
    void cleanup(){
        sheetBookingRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();

        instrument1 = new Instrument();
        instrument1.setName("Guitar");
        instrument1.setDescription("Electric Guitar");
        // create users for owners and renters
        owner10 = userRepository.save(new com.example.OLSHEETS.data.User("owner10"));
        owner20 = userRepository.save(new com.example.OLSHEETS.data.User("owner20"));
        renter100 = userRepository.save(new com.example.OLSHEETS.data.User("renter100"));
        renter200 = userRepository.save(new com.example.OLSHEETS.data.User("renter200"));
        // owner will be set to a persisted User
        instrument1.setOwner(owner10);
        instrument1.setPrice(50.0);
        instrument1.setAge(2);
        instrument1.setType(InstrumentType.ELECTRIC);
        instrument1.setFamily(InstrumentFamily.STRING);
        instrument1 = itemRepository.save(instrument1);

        instrument2 = new Instrument();
        instrument2.setName("Piano");
        instrument2.setDescription("Digital Piano");
        owner20 = userRepository.save(new com.example.OLSHEETS.data.User("owner20"));
        instrument2.setOwner(owner20);
        instrument2.setPrice(100.0);
        instrument2.setAge(1);
        instrument2.setType(InstrumentType.DIGITAL);
        instrument2.setFamily(InstrumentFamily.KEYBOARD);
        instrument2 = itemRepository.save(instrument2);
    }

    @Test
    void shouldCreateBookingWhenNoOverlap() {
        com.example.OLSHEETS.data.User u = userRepository.save(new com.example.OLSHEETS.data.User("u100"));

        Booking b = bookingService.createBooking(instrument1.getId(), u.getId(), LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5));
        assertThat(b.getId()).isNotNull();
        assertThat(b.getItem().getId()).isEqualTo(instrument1.getId());
        assertThat(b.getItem().getOwner().getId()).isEqualTo(instrument1.getOwner().getId());
        assertThat(b.getRenter().getId()).isEqualTo(u.getId());
    }

    @Test
    void shouldRejectOverlappingBooking() {
        bookingService.createBooking(instrument2.getId(), renter100.getId(), LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));

        assertThatThrownBy(() -> bookingService.createBooking(instrument2.getId(), renter200.getId(), LocalDate.of(2025, 12, 14),
            LocalDate.of(2025, 12, 20))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldApproveBookingWhenOwnerIsAuthorized() {
        Booking booking = bookingService.createBooking(instrument1.getId(), renter100.getId(), LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5));
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

        Booking approved = bookingService.approveBooking(booking.getId(), owner10.getId().intValue());

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(approved.getId()).isEqualTo(booking.getId());
    }

    @Test
    void shouldRejectBookingWhenOwnerIsAuthorized() {
        Booking booking = bookingService.createBooking(instrument1.getId(), renter100.getId(), LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5));
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

        Booking rejected = bookingService.rejectBooking(booking.getId(), 10);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(rejected.getId()).isEqualTo(booking.getId());
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerTriesToApprove() {
        Booking booking = bookingService.createBooking(instrument1.getId(), renter100.getId(), LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5));

        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), 999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not authorized");

        Booking found = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerTriesToReject() {
        Booking booking = bookingService.createBooking(instrument1.getId(), renter100.getId(), LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5));

        assertThatThrownBy(() -> bookingService.rejectBooking(booking.getId(), 999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not authorized");

        Booking found = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void shouldThrowExceptionWhenApprovingNonExistentBooking() {
        assertThatThrownBy(() -> bookingService.approveBooking(999L, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void shouldThrowExceptionWhenRejectingNonExistentBooking() {
        assertThatThrownBy(() -> bookingService.rejectBooking(999L, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void shouldThrowExceptionWhenApprovingAlreadyApprovedBooking() {
        Booking booking = bookingService.createBooking(instrument1.getId(), renter100.getId(), LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5));
        bookingService.approveBooking(booking.getId(), owner10.getId().intValue());

        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), owner10.getId().intValue()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been approved");
    }

    @Test
    void shouldThrowExceptionWhenRejectingAlreadyRejectedBooking() {
        Booking booking = bookingService.createBooking(instrument1.getId(), renter100.getId(), LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5));
        bookingService.rejectBooking(booking.getId(), owner10.getId().intValue());

        assertThatThrownBy(() -> bookingService.rejectBooking(booking.getId(), owner10.getId().intValue()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been rejected");
    }
}
