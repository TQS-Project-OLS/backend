package com.example.OLSHEETS.dto;

/**
 * DTO for creating a new renter review.
 */
public class RenterReviewRequest extends BaseReviewRequest {

    public RenterReviewRequest() {
        super();
    }

    public RenterReviewRequest(Long bookingId, Integer score, String comment) {
        super(bookingId, score, comment);
    }
}
