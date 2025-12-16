package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.*;
import com.example.OLSHEETS.dto.ReviewRequest;
import com.example.OLSHEETS.dto.ReviewResponse;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.repository.ReviewRepository;
import com.example.OLSHEETS.service.ReviewService;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User renter;
    private Instrument instrument;
    private Booking booking;
    private Review review;

    @BeforeEach
    void setUp() {
        renter = new User("john_doe", "john@example.com", "John Doe", "password123");
        renter.setId(1L);

        instrument = new Instrument();
        instrument.setId(1L);
        instrument.setName("Yamaha Piano");
        instrument.setPrice(599.99);

        booking = new Booking(instrument, renter, LocalDate.now().minusDays(10), LocalDate.now().minusDays(3));
        booking.setId(1L);
        booking.setStatus(BookingStatus.APPROVED);

        review = new Review(booking, 5, "Excellent instrument!");
        review.setId(1L);
        review.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateReview_WithValidData_ShouldSucceed() {
        ReviewRequest request = new ReviewRequest(1L, 5, "Great experience!");
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBooking(booking)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.createReview(request, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(5, response.getScore());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testCreateReview_WithInvalidScore_ShouldThrowException() {
        ReviewRequest request = new ReviewRequest(1L, 6, "Invalid score");

        assertThrows(IllegalArgumentException.class, () -> 
            reviewService.createReview(request, 1L)
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testCreateReview_WithScoreBelowOne_ShouldThrowException() {
        ReviewRequest request = new ReviewRequest(1L, 0, "Invalid score");

        assertThrows(IllegalArgumentException.class, () -> 
            reviewService.createReview(request, 1L)
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testCreateReview_WithNonExistentBooking_ShouldThrowException() {
        ReviewRequest request = new ReviewRequest(999L, 5, "Great!");
        
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            reviewService.createReview(request, 1L)
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testCreateReview_WithWrongRenter_ShouldThrowException() {
        ReviewRequest request = new ReviewRequest(1L, 5, "Great!");
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class, () -> 
            reviewService.createReview(request, 999L)
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testCreateReview_BeforeBookingEnds_ShouldThrowException() {
        Booking futureBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(2), LocalDate.now().plusDays(2));
        futureBooking.setId(2L);
        
        ReviewRequest request = new ReviewRequest(2L, 5, "Too early!");
        
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(futureBooking));

        assertThrows(IllegalArgumentException.class, () -> 
            reviewService.createReview(request, 1L)
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testCreateReview_WhenReviewAlreadyExists_ShouldThrowException() {
        ReviewRequest request = new ReviewRequest(1L, 5, "Great!");
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBooking(booking)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> 
            reviewService.createReview(request, 1L)
        );

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testGetReviewsByItemId_ShouldReturnReviews() {
        Review review2 = new Review(booking, 4, "Good!");
        review2.setId(2L);
        review2.setCreatedAt(LocalDateTime.now());
        
        when(reviewRepository.findByItemId(1L)).thenReturn(Arrays.asList(review, review2));

        List<ReviewResponse> reviews = reviewService.getReviewsByItemId(1L);

        assertEquals(2, reviews.size());
        verify(reviewRepository, times(1)).findByItemId(1L);
    }

    @Test
    void testGetAverageScoreByItemId_ShouldReturnAverage() {
        when(reviewRepository.getAverageScoreByItemId(1L)).thenReturn(4.5);

        Double average = reviewService.getAverageScoreByItemId(1L);

        assertEquals(4.5, average);
        verify(reviewRepository, times(1)).getAverageScoreByItemId(1L);
    }

    @Test
    void testGetAverageScoreByItemId_WithNoReviews_ShouldReturnZero() {
        when(reviewRepository.getAverageScoreByItemId(1L)).thenReturn(null);

        Double average = reviewService.getAverageScoreByItemId(1L);

        assertEquals(0.0, average);
    }

    @Test
    void testCanReviewBooking_WithValidBooking_ShouldReturnTrue() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBooking(booking)).thenReturn(false);

        boolean canReview = reviewService.canReviewBooking(1L, 1L);

        assertTrue(canReview);
    }

    @Test
    void testCanReviewBooking_WithNonExistentBooking_ShouldReturnFalse() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        boolean canReview = reviewService.canReviewBooking(999L, 1L);

        assertFalse(canReview);
    }

    @Test
    void testCanReviewBooking_WithWrongRenter_ShouldReturnFalse() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        boolean canReview = reviewService.canReviewBooking(1L, 999L);

        assertFalse(canReview);
    }

    @Test
    void testCanReviewBooking_BeforeBookingEnds_ShouldReturnFalse() {
        Booking futureBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(2), LocalDate.now().plusDays(2));
        futureBooking.setId(2L);
        
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(futureBooking));

        boolean canReview = reviewService.canReviewBooking(2L, 1L);

        assertFalse(canReview);
    }

    @Test
    void testCanReviewBooking_WhenReviewExists_ShouldReturnFalse() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBooking(booking)).thenReturn(true);

        boolean canReview = reviewService.canReviewBooking(1L, 1L);

        assertFalse(canReview);
    }

    @Test
    void testGetReviewByBookingId_WithExistingReview_ShouldReturnReview() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.findByBooking(booking)).thenReturn(Optional.of(review));

        ReviewResponse response = reviewService.getReviewByBookingId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(5, response.getScore());
    }

    @Test
    void testGetReviewByBookingId_WithNonExistentBooking_ShouldThrowException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            reviewService.getReviewByBookingId(999L)
        );
    }

    @Test
    void testGetReviewByBookingId_WithNoReview_ShouldThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.findByBooking(booking)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            reviewService.getReviewByBookingId(1L)
        );
    }

    @Test
    void testCreateReview_WithRejectedBooking_ShouldThrowException() {
        Booking rejectedBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(10), LocalDate.now().minusDays(3));
        rejectedBooking.setId(2L);
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        
        ReviewRequest request = new ReviewRequest(2L, 5, "Great!");
        
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(rejectedBooking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            reviewService.createReview(request, 1L)
        );

        assertEquals("Can only review approved bookings", exception.getMessage());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testCreateReview_WithCancelledBooking_ShouldThrowException() {
        Booking cancelledBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(10), LocalDate.now().minusDays(3));
        cancelledBooking.setId(3L);
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        
        ReviewRequest request = new ReviewRequest(3L, 5, "Great!");
        
        when(bookingRepository.findById(3L)).thenReturn(Optional.of(cancelledBooking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            reviewService.createReview(request, 1L)
        );

        assertEquals("Can only review approved bookings", exception.getMessage());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testCreateReview_WithPendingBooking_ShouldThrowException() {
        Booking pendingBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(10), LocalDate.now().minusDays(3));
        pendingBooking.setId(4L);
        pendingBooking.setStatus(BookingStatus.PENDING);
        
        ReviewRequest request = new ReviewRequest(4L, 5, "Great!");
        
        when(bookingRepository.findById(4L)).thenReturn(Optional.of(pendingBooking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            reviewService.createReview(request, 1L)
        );

        assertEquals("Can only review approved bookings", exception.getMessage());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void testCreateReview_WithApprovedBooking_ShouldSucceed() {
        Booking approvedBooking = new Booking(instrument, renter, 
            LocalDate.now().minusDays(10), LocalDate.now().minusDays(3));
        approvedBooking.setId(5L);
        approvedBooking.setStatus(BookingStatus.APPROVED);
        
        ReviewRequest request = new ReviewRequest(5L, 5, "Excellent!");
        
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(approvedBooking));
        when(reviewRepository.existsByBooking(approvedBooking)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.createReview(request, 1L);

        assertNotNull(response);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }
}
