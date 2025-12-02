package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingRepository;
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
        Booking b = bookingService.createBooking(1L, LocalDate.of(2025,12,1), LocalDate.of(2025,12,5));
        assertThat(b.getId()).isNotNull();
        assertThat(b.getInstrumentId()).isEqualTo(1L);
    }

    @Test
    void shouldRejectOverlappingBooking(){
        bookingService.createBooking(2L, LocalDate.of(2025,12,10), LocalDate.of(2025,12,15));

        assertThatThrownBy(() ->
                bookingService.createBooking(2L, LocalDate.of(2025,12,14), LocalDate.of(2025,12,20))
        ).isInstanceOf(IllegalStateException.class);
    }

}
