package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetBooking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.SheetCategory;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import com.example.OLSHEETS.service.SheetBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SheetBookingServiceTest {

    @Mock
    private SheetBookingRepository bookingRepository;

    @Mock
    private MusicSheetRepository sheetRepository;

    @InjectMocks
    private SheetBookingService bookingService;

    private MusicSheet sheet;
    private SheetBooking booking;

    @BeforeEach
    void setUp() {
        sheet = new MusicSheet();
        sheet.setId(1L);
        sheet.setName("Moonlight Sonata");
        sheet.setComposer("Beethoven");
        sheet.setCategory(SheetCategory.CLASSICAL);
        sheet.setDescription("Beautiful piece");
        sheet.setPrice(5.00);
        sheet.setOwnerId(1);

        booking = new SheetBooking(sheet, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setId(1L);
    }

    @Test
    void whenCreateValidBooking_thenSuccess() {
        when(sheetRepository.findById(1L)).thenReturn(Optional.of(sheet));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any())).thenReturn(List.of());
        when(bookingRepository.save(any(SheetBooking.class))).thenReturn(booking);

        SheetBooking created = bookingService.createBooking(1L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertThat(created).isNotNull();
        assertThat(created.getRenterId()).isEqualTo(100L);
        assertThat(created.getStatus()).isEqualTo(BookingStatus.PENDING);
        verify(bookingRepository, times(1)).save(any(SheetBooking.class));
    }

    @Test
    void whenCreateBookingForNonExistentSheet_thenThrowException() {
        when(sheetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(999L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Music sheet not found");
    }

    @Test
    void whenCreateBookingWithConflict_thenThrowException() {
        when(sheetRepository.findById(1L)).thenReturn(Optional.of(sheet));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any())).thenReturn(List.of(booking));

        assertThatThrownBy(() -> bookingService.createBooking(1L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already booked");
    }

    @Test
    void whenGetBookingsByRenter_thenReturnBookings() {
        when(bookingRepository.findByRenterId(100L)).thenReturn(List.of(booking));

        List<SheetBooking> bookings = bookingService.getBookingsByRenter(100L);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getRenterId()).isEqualTo(100L);
    }

    @Test
    void whenGetBookingById_thenReturnBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Optional<SheetBooking> found = bookingService.getBookingById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(1L);
    }

    @Test
    void whenInvalidDateRange_thenThrowException() {
        assertThatThrownBy(() -> bookingService.createBooking(1L, 100L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(2)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Start date must be before end date");
    }
}
