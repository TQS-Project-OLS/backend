package com.example.OLSHEETS.controller;

import com.example.OLSHEETS.dto.RenterReviewRequest;
import com.example.OLSHEETS.dto.RenterReviewResponse;
import com.example.OLSHEETS.service.RenterReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/renter-reviews")
public class RenterReviewController {

    private final RenterReviewService renterReviewService;

    public RenterReviewController(RenterReviewService renterReviewService) {
        this.renterReviewService = renterReviewService;
    }

    /**
     * Create a new renter review
     * POST /api/renter-reviews?ownerId={id}
     */
    @PostMapping
    public ResponseEntity<?> createRenterReview(
            @RequestBody RenterReviewRequest request,
            @RequestParam Long ownerId) {
        try {
            RenterReviewResponse response = renterReviewService.createRenterReview(request, ownerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get all reviews for a specific renter
     * GET /api/renter-reviews/renter/{renterId}
     */
    @GetMapping("/renter/{renterId}")
    public ResponseEntity<List<RenterReviewResponse>> getReviewsByRenterId(@PathVariable Long renterId) {
        List<RenterReviewResponse> reviews = renterReviewService.getReviewsByRenterId(renterId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get average score for a renter
     * GET /api/renter-reviews/renter/{renterId}/average
     */
    @GetMapping("/renter/{renterId}/average")
    public ResponseEntity<Double> getAverageScoreByRenterId(@PathVariable Long renterId) {
        Double average = renterReviewService.getAverageScoreByRenterId(renterId);
        return ResponseEntity.ok(average);
    }

    /**
     * Get a renter review by booking ID
     * GET /api/renter-reviews/booking/{bookingId}
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getRenterReviewByBookingId(@PathVariable Long bookingId) {
        try {
            RenterReviewResponse response = renterReviewService.getRenterReviewByBookingId(bookingId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Check if a renter can be reviewed for a booking
     * GET /api/renter-reviews/can-review?bookingId={id}&ownerId={id}
     */
    @GetMapping("/can-review")
    public ResponseEntity<Boolean> canReviewRenter(
            @RequestParam Long bookingId,
            @RequestParam Long ownerId) {
        boolean canReview = renterReviewService.canReviewRenter(bookingId, ownerId);
        return ResponseEntity.ok(canReview);
    }
}
