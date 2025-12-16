package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.*;
import com.example.OLSHEETS.dto.RenterReviewRequest;
import com.example.OLSHEETS.dto.RenterReviewResponse;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.repository.RenterReviewRepository;
import com.example.OLSHEETS.service.RenterReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenterReviewServiceTest {

    @Mock
    private RenterReviewRepository renterReviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RenterReviewService renterReviewService;

    private User owner;
    private User renter;
    private Instrument instrument;
    private Booking booking;
    private RenterReview renterReview;

    @BeforeEach
    void setUp() {
        owner = new User("owner_user", "owner@example.com", "Owner User", "password123");
        owner.setId(1L);

        renter = new User("renter_user", "renter@example.com", "Renter User", "password123");
        renter.setId(2L);

        instrument = new Instrument();
        instrument.setId(1L);
        instrument.setName("Yamaha Piano");
        instrument.setPrice(599.99);
        instrument.setOwner(owner);

        booking = new Booking(instrument, renter, LocalDate.now().minusDays(10), LocalDate.now().minusDays(3));
        booking.setId(1L);
        booking.setStatus(BookingStatus.APPROVED);

        renterReview = new RenterReview(booking, 5, "Excellent renter!");
        renterReview.setId(1L);
        renterReview.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateRenterReview_WithValidData_ShouldSucceed() {
        RenterReviewRequest request = new RenterReviewRequest(1L, 5, "Great renter!");
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(renterReviewRepository.existsByBooking(booking)).thenReturn(false);
        when(renterReviewRepository.save(any(RenterReview.class))).thenReturn(renterReview);

        RenterReviewResponse response = renterReviewService.createRenterReview(request, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(5, response.getScore());
        verify(renterReviewRepository, times(1)).save(any(RenterReview.class));
    }

    @Test
    void testCreateRenterReview_WithInvalidScore_ShouldThrowException() {
        RenterReviewRequest request = new RenterReviewRequest(1L, 6, "Invalid score");

        assertThrows(IllegalArgumentException.class, () -> 
            renterReviewService.createRenterReview(request, 1L)
        );

        verify(renterReviewRepository, never()).save(any(RenterReview.class));
    }

    @Test
    void testCreateRenterReview_WithScoreBelowOne_ShouldThrowException() {
        RenterReviewRequest request = new RenterReviewRequest(1L, 0, "Invalid score");

        assertThrows(IllegalArgumentException.class, () -> 
            renterReviewService.createRenterReview(request, 1L)
        );

        verify(renterReviewRepository, never()).save(any(RenterReview.class));
    }

    @Test
    void testCreateRenterReview_WithNonExistentBooking_ShouldThrowException() {
        RenterReviewRequest request = new RenterReviewRequest(999L, 5, "Great!");
        
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            renterReviewService.createRenterReview(request, 1L)
        );

        verify(renterReviewRepository, never()).save(any(RenterReview.class));
    }

    @Test
    void testCreateRenterReview_WithWrongOwner_ShouldThrowException() {
        RenterReviewRequest request = new RenterReviewRequest(1L, 5, "Great!");
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class, () -> 
            renterReviewService.createRenterReview(request, 999L)
        );

        verify(renterReviewRepository, never()).save(any(RenterReview.class));
    }

    @Test
    void testCreateRenterReview_BeforeBookingEnds_ShouldThrowException() {
        Booking futureBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(2), LocalDate.now().plusDays(2));
        futureBooking.setId(2L);
        
        RenterReviewRequest request = new RenterReviewRequest(2L, 5, "Too early!");
        
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(futureBooking));

        assertThrows(IllegalArgumentException.class, () -> 
            renterReviewService.createRenterReview(request, 1L)
        );

        verify(renterReviewRepository, never()).save(any(RenterReview.class));
    }

    @Test
    void testCreateRenterReview_WhenReviewAlreadyExists_ShouldThrowException() {
        RenterReviewRequest request = new RenterReviewRequest(1L, 5, "Great!");
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(renterReviewRepository.existsByBooking(booking)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> 
            renterReviewService.createRenterReview(request, 1L)
        );

        verify(renterReviewRepository, never()).save(any(RenterReview.class));
    }

    @Test
    void testGetReviewsByRenterId_ShouldReturnReviews() {
        RenterReview review2 = new RenterReview(booking, 4, "Good!");
        review2.setId(2L);
        review2.setCreatedAt(LocalDateTime.now());
        
        when(renterReviewRepository.findByRenterId(2L)).thenReturn(Arrays.asList(renterReview, review2));

        List<RenterReviewResponse> reviews = renterReviewService.getReviewsByRenterId(2L);

        assertEquals(2, reviews.size());
        verify(renterReviewRepository, times(1)).findByRenterId(2L);
    }

    @Test
    void testGetAverageScoreByRenterId_ShouldReturnAverage() {
        when(renterReviewRepository.getAverageScoreByRenterId(2L)).thenReturn(4.5);

        Double average = renterReviewService.getAverageScoreByRenterId(2L);

        assertEquals(4.5, average);
        verify(renterReviewRepository, times(1)).getAverageScoreByRenterId(2L);
    }

    @Test
    void testGetAverageScoreByRenterId_WithNoReviews_ShouldReturnZero() {
        when(renterReviewRepository.getAverageScoreByRenterId(2L)).thenReturn(null);

        Double average = renterReviewService.getAverageScoreByRenterId(2L);

        assertEquals(0.0, average);
    }

    @Test
    void testCanReviewRenter_WithValidBooking_ShouldReturnTrue() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(renterReviewRepository.existsByBooking(booking)).thenReturn(false);

        boolean canReview = renterReviewService.canReviewRenter(1L, 1L);

        assertTrue(canReview);
    }

    @Test
    void testCanReviewRenter_WithNonExistentBooking_ShouldReturnFalse() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        boolean canReview = renterReviewService.canReviewRenter(999L, 1L);

        assertFalse(canReview);
    }

    @Test
    void testCanReviewRenter_WithWrongOwner_ShouldReturnFalse() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        boolean canReview = renterReviewService.canReviewRenter(1L, 999L);

        assertFalse(canReview);
    }

    @Test
    void testCanReviewRenter_BeforeBookingEnds_ShouldReturnFalse() {
        Booking futureBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(2), LocalDate.now().plusDays(2));
        futureBooking.setId(2L);
        
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(futureBooking));

        boolean canReview = renterReviewService.canReviewRenter(2L, 1L);

        assertFalse(canReview);
    }

    @Test
    void testCanReviewRenter_WhenReviewExists_ShouldReturnFalse() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(renterReviewRepository.existsByBooking(booking)).thenReturn(true);

        boolean canReview = renterReviewService.canReviewRenter(1L, 1L);

        assertFalse(canReview);
    }

    @Test
    void testGetRenterReviewByBookingId_WithExistingReview_ShouldReturnReview() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(renterReviewRepository.findByBooking(booking)).thenReturn(Optional.of(renterReview));

        RenterReviewResponse response = renterReviewService.getRenterReviewByBookingId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(5, response.getScore());
        verify(bookingRepository, times(1)).findById(1L);
        verify(renterReviewRepository, times(1)).findByBooking(booking);
    }

    @Test
    void testGetRenterReviewByBookingId_WithNonExistentBooking_ShouldThrowException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            renterReviewService.getRenterReviewByBookingId(999L)
        );

        verify(renterReviewRepository, never()).findByBooking(any());
    }

    @Test
    void testGetRenterReviewByBookingId_WithNoReview_ShouldThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(renterReviewRepository.findByBooking(booking)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            renterReviewService.getRenterReviewByBookingId(1L)
        );
    }

    @Test
    void testCreateRenterReview_WithRejectedBooking_ShouldThrowException() {
        Booking rejectedBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(10), LocalDate.now().minusDays(3));
        rejectedBooking.setId(2L);
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        
        RenterReviewRequest request = new RenterReviewRequest(2L, 5, "Great!");
        
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(rejectedBooking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            renterReviewService.createRenterReview(request, 1L)
        );

        assertEquals("Can only review approved bookings", exception.getMessage());
        verify(renterReviewRepository, never()).save(any(RenterReview.class));
    }

    @Test
    void testCreateRenterReview_WithCancelledBooking_ShouldThrowException() {
        Booking cancelledBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(10), LocalDate.now().minusDays(3));
        cancelledBooking.setId(3L);
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        
        RenterReviewRequest request = new RenterReviewRequest(3L, 5, "Great!");
        
        when(bookingRepository.findById(3L)).thenReturn(Optional.of(cancelledBooking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            renterReviewService.createRenterReview(request, 1L)
        );

        assertEquals("Can only review approved bookings", exception.getMessage());
        verify(renterReviewRepository, never()).save(any(RenterReview.class));
    }

    @Test
    void testCreateRenterReview_WithPendingBooking_ShouldThrowException() {
        Booking pendingBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(10), LocalDate.now().minusDays(3));
        pendingBooking.setId(4L);
        pendingBooking.setStatus(BookingStatus.PENDING);
        
        RenterReviewRequest request = new RenterReviewRequest(4L, 5, "Great!");
        
        when(bookingRepository.findById(4L)).thenReturn(Optional.of(pendingBooking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            renterReviewService.createRenterReview(request, 1L)
        );

        assertEquals("Can only review approved bookings", exception.getMessage());
        verify(renterReviewRepository, never()).save(any(RenterReview.class));
    }

    @Test
    void testCreateRenterReview_WithApprovedBooking_ShouldSucceed() {
        Booking approvedBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(10), LocalDate.now().minusDays(3));
        approvedBooking.setId(5L);
        approvedBooking.setStatus(BookingStatus.APPROVED);
        
        RenterReviewRequest request = new RenterReviewRequest(5L, 5, "Excellent!");
        
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(approvedBooking));
        when(renterReviewRepository.existsByBooking(approvedBooking)).thenReturn(false);
        when(renterReviewRepository.save(any(RenterReview.class))).thenReturn(renterReview);

        RenterReviewResponse response = renterReviewService.createRenterReview(request, 1L);

        assertNotNull(response);
        verify(renterReviewRepository, times(1)).save(any(RenterReview.class));
    }
}
