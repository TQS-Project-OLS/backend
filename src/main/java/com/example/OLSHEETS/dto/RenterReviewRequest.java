package com.example.OLSHEETS.dto;

public class RenterReviewRequest {
    private Long bookingId;
    private Integer score;
    private String comment;

    public RenterReviewRequest() {
        // Default constructor for JSON deserialization
    }

    public RenterReviewRequest(Long bookingId, Integer score, String comment) {
        this.bookingId = bookingId;
        this.score = score;
        this.comment = comment;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
