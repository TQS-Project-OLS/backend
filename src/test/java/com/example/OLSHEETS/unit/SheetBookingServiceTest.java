package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetBooking;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import com.example.OLSHEETS.service.SheetBookingService;
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
class SheetBookingServiceTest {

    @Mock
    private SheetBookingRepository bookingRepository;

    @Mock
    private MusicSheetRepository sheetRepository;

    @Mock
    private com.example.OLSHEETS.repository.UserRepository userRepository;

    @InjectMocks
    private SheetBookingService bookingService;

    private MusicSheet sheet;
    private SheetBooking booking;
    private com.example.OLSHEETS.data.User testUser;

    @BeforeEach
    void setUp() {
        User owner = new User("owner1", "owner1@example.com", "Owner One", "password123");
        owner.setId(1L);
        sheet = new MusicSheet();
        sheet.setTitle("Moonlight Sonata");
        sheet.setCategory("classical");
        sheet.setComposer("Beautiful piece");
        sheet.setPrice(5.00);
        sheet.setOwner(owner);
        sheet.setId(1L);

        testUser = new com.example.OLSHEETS.data.User("tester", "tester@example.com", "Test User");
        testUser.setId(100L);

        booking = new SheetBooking(sheet, testUser, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setId(1L);
    }

    @Test
    void whenCreateValidBooking_thenSuccess() {
        when(sheetRepository.findById(1L)).thenReturn(Optional.of(sheet));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any())).thenReturn(List.of());
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(bookingRepository.save(any(SheetBooking.class))).thenReturn(booking);

        SheetBooking created = bookingService.createBooking(1L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertThat(created).isNotNull();
        assertThat(created.getRenter().getId()).isEqualTo(100L);
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
        assertThat(bookings.get(0).getRenter().getId()).isEqualTo(100L);
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

    @Test
    void whenStartDateEqualsEndDate_thenThrowException() {
        LocalDate sameDate = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> bookingService.createBooking(1L, 100L, sameDate, sameDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Start date must be before end date");
    }

    @Test
    void whenGetAllBookings_thenReturnAllBookings() {
        User user2 = new User("tester2", "tester2@example.com", "Test User Two", "password123");
        SheetBooking booking2 = new SheetBooking(sheet, user2, LocalDate.now().plusDays(5), LocalDate.now().plusDays(7));
        when(bookingRepository.findAll()).thenReturn(List.of(booking, booking2));

        List<SheetBooking> bookings = bookingService.getAllBookings();

        assertThat(bookings).hasSize(2);
        assertThat(bookings).containsExactly(booking, booking2);
        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    void whenGetBookingsBySheet_thenReturnBookings() {
        when(sheetRepository.findById(1L)).thenReturn(Optional.of(sheet));
        when(bookingRepository.findByMusicSheet(sheet)).thenReturn(List.of(booking));

        List<SheetBooking> bookings = bookingService.getBookingsBySheet(1L);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).isEqualTo(booking);
        verify(sheetRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).findByMusicSheet(sheet);
    }

    @Test
    void whenGetBookingsBySheetForNonExistentSheet_thenThrowException() {
        when(sheetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingsBySheet(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Music sheet not found with id: 999");
    }

    @Test
    void whenGetBookingByIdNotFound_thenReturnEmpty() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<SheetBooking> found = bookingService.getBookingById(999L);

        assertThat(found).isEmpty();
        verify(bookingRepository, times(1)).findById(999L);
    }

    @Test
    void whenGetBookingsByRenterWithNoBookings_thenReturnEmptyList() {
        when(bookingRepository.findByRenterId(999L)).thenReturn(List.of());

        List<SheetBooking> bookings = bookingService.getBookingsByRenter(999L);

        assertThat(bookings).isEmpty();
        verify(bookingRepository, times(1)).findByRenterId(999L);
    }
}
