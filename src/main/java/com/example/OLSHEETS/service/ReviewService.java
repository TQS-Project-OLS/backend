package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.Review;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.dto.ReviewRequest;
import com.example.OLSHEETS.dto.ReviewResponse;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Create a review for a booking
     * Validates:
     * - Booking exists
     * - Booking has ended
     * - User is the renter of the booking
     * - No review exists for this booking
     * - Score is between 1 and 5
     */
    @Transactional
    public ReviewResponse createReview(ReviewRequest request, Long renterId) {
        // Validate score range
        if (request.getScore() == null || request.getScore() < 1 || request.getScore() > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5");
        }

        // Find the booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + request.getBookingId()));

        // Validate the renter is the one who made the booking
        if (!booking.getRenter().getId().equals(renterId)) {
            throw new IllegalArgumentException("You can only review your own bookings");
        }

        // Validate booking has ended
        if (booking.getEndDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot review a booking that hasn't ended yet");
        }

        // Validate booking was approved (only approved bookings can be reviewed)
        if (booking.getStatus() != com.example.OLSHEETS.data.BookingStatus.APPROVED) {
            throw new IllegalArgumentException("Can only review approved bookings");
        }

        // Check if review already exists
        if (reviewRepository.existsByBooking(booking)) {
            throw new IllegalArgumentException("A review already exists for this booking");
        }

        // Create and save review
        Review review = new Review(booking, request.getScore(), request.getComment());
        Review savedReview = reviewRepository.save(review);

        return mapToResponse(savedReview);
    }

    /**
     * Get all reviews for a specific item
     */
    public List<ReviewResponse> getReviewsByItemId(Long itemId) {
        List<Review> reviews = reviewRepository.findByItemId(itemId);
        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get average score for an item
     */
    public Double getAverageScoreByItemId(Long itemId) {
        Double average = reviewRepository.getAverageScoreByItemId(itemId);
        return average != null ? average : 0.0;
    }

    /**
     * Get a review by booking ID
     */
    public ReviewResponse getReviewByBookingId(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));
        
        Review review = reviewRepository.findByBooking(booking)
                .orElseThrow(() -> new IllegalArgumentException("No review found for this booking"));
        
        return mapToResponse(review);
    }

    /**
     * Check if a booking can be reviewed
     */
    public boolean canReviewBooking(Long bookingId, Long renterId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return false;
        }

        // Check if user is the renter
        if (!booking.getRenter().getId().equals(renterId)) {
            return false;
        }

        // Check if booking has ended
        if (booking.getEndDate().isAfter(LocalDate.now())) {
            return false;
        }

        // Check if review already exists
        return !reviewRepository.existsByBooking(booking);
    }

    /**
     * Map Review entity to ReviewResponse DTO
     */
    private ReviewResponse mapToResponse(Review review) {
        Booking booking = review.getBooking();
        User renter = booking.getRenter();
        
        return new ReviewResponse(
                review.getId(),
                booking.getId(),
                booking.getItem().getId(),
                booking.getItem().getName(),
                renter.getId(),
                renter.getUsername(),
                review.getScore(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
