package com.example.OLSHEETS.dto;

/**
 * Base class for review request DTOs containing common fields.
 */
public abstract class BaseReviewRequest {

    private Long bookingId;
    private Integer score;
    private String comment;

    protected BaseReviewRequest() {
    }

    protected BaseReviewRequest(Long bookingId, Integer score, String comment) {
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
