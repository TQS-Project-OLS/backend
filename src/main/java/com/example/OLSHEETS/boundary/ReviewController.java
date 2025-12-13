package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.dto.ReviewRequest;
import com.example.OLSHEETS.dto.ReviewResponse;
import com.example.OLSHEETS.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Create a review for a booking
     * POST /api/reviews
     * 
     * Request body:
     * {
     *   "bookingId": 1,
     *   "score": 5,
     *   "comment": "Great instrument!"
     * }
     * 
     * For now, renterId is passed as a request parameter.
     * In a real application, this would come from the authenticated user.
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @RequestBody ReviewRequest request,
            @RequestParam Long renterId) {
        try {
            ReviewResponse response = reviewService.createReview(request, renterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all reviews for a specific item
     * GET /api/reviews/item/{itemId}
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByItem(@PathVariable Long itemId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByItemId(itemId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get average score for an item
     * GET /api/reviews/item/{itemId}/average
     */
    @GetMapping("/item/{itemId}/average")
    public ResponseEntity<Map<String, Double>> getAverageScore(@PathVariable Long itemId) {
        Double average = reviewService.getAverageScoreByItemId(itemId);
        return ResponseEntity.ok(Map.of("averageScore", average));
    }

    /**
     * Get review for a specific booking
     * GET /api/reviews/booking/{bookingId}
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ReviewResponse> getReviewByBooking(@PathVariable Long bookingId) {
        try {
            ReviewResponse review = reviewService.getReviewByBookingId(bookingId);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if a booking can be reviewed
     * GET /api/reviews/booking/{bookingId}/can-review
     */
    @GetMapping("/booking/{bookingId}/can-review")
    public ResponseEntity<Map<String, Boolean>> canReview(
            @PathVariable Long bookingId,
            @RequestParam Long renterId) {
        boolean canReview = reviewService.canReviewBooking(bookingId, renterId);
        return ResponseEntity.ok(Map.of("canReview", canReview));
    }
}
