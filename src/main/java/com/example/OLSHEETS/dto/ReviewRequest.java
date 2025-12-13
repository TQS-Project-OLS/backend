package com.example.OLSHEETS.dto;

/**
 * DTO for creating a new review.
 */
public class ReviewRequest extends BaseReviewRequest {

    public ReviewRequest() {
        super();
    }

    public ReviewRequest(Long bookingId, Integer score, String comment) {
        super(bookingId, score, comment);
    }
}
