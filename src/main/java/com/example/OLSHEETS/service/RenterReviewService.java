package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.RenterReview;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.dto.RenterReviewRequest;
import com.example.OLSHEETS.dto.RenterReviewResponse;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.repository.RenterReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RenterReviewService {

    private final RenterReviewRepository renterReviewRepository;
    private final BookingRepository bookingRepository;

    public RenterReviewService(RenterReviewRepository renterReviewRepository, BookingRepository bookingRepository) {
        this.renterReviewRepository = renterReviewRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Create a review for a renter
     * Validates:
     * - Booking exists
     * - Booking has ended
     * - User is the owner of the item
     * - No review exists for this booking
     * - Score is between 1 and 5
     */
    @Transactional
    public RenterReviewResponse createRenterReview(RenterReviewRequest request, Long ownerId) {
        // Validate score range
        if (request.getScore() == null || request.getScore() < 1 || request.getScore() > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5");
        }

        // Find the booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + request.getBookingId()));

        // Validate the owner is the one who owns the item
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("You can only review renters of your own items");
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
        if (renterReviewRepository.existsByBooking(booking)) {
            throw new IllegalArgumentException("A review already exists for this booking");
        }

        // Create and save review
        RenterReview review = new RenterReview(booking, request.getScore(), request.getComment());
        RenterReview savedReview = renterReviewRepository.save(review);

        return mapToResponse(savedReview);
    }

    /**
     * Get all reviews for a specific renter
     */
    public List<RenterReviewResponse> getReviewsByRenterId(Long renterId) {
        List<RenterReview> reviews = renterReviewRepository.findByRenterId(renterId);
        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get average score for a renter
     */
    public Double getAverageScoreByRenterId(Long renterId) {
        Double average = renterReviewRepository.getAverageScoreByRenterId(renterId);
        return average != null ? average : 0.0;
    }

    /**
     * Get a renter review by booking ID
     */
    public RenterReviewResponse getRenterReviewByBookingId(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));
        
        RenterReview review = renterReviewRepository.findByBooking(booking)
                .orElseThrow(() -> new IllegalArgumentException("No review found for this booking"));
        
        return mapToResponse(review);
    }

    /**
     * Check if a renter can be reviewed for a booking
     */
    public boolean canReviewRenter(Long bookingId, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return false;
        }

        // Check if user is the owner
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            return false;
        }

        // Check if booking has ended
        if (booking.getEndDate().isAfter(LocalDate.now())) {
            return false;
        }

        // Check if review already exists
        return !renterReviewRepository.existsByBooking(booking);
    }

    /**
     * Map RenterReview entity to RenterReviewResponse DTO
     */
    private RenterReviewResponse mapToResponse(RenterReview review) {
        Booking booking = review.getBooking();
        User renter = booking.getRenter();
        
        return new RenterReviewResponse(
                review.getId(),
                booking.getId(),
                renter.getId(),
                renter.getUsername(),
                booking.getItem().getId(),
                booking.getItem().getName(),
                booking.getItem().getOwner().getId(),
                review.getScore(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
