package com.example.OLSHEETS.dto;

import java.time.LocalDateTime;

public class ReviewResponse {
    private Long id;
    private Long bookingId;
    private Long itemId;
    private String itemName;
    private Long renterId;
    private String renterName;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewResponse() {
        // Default constructor for JSON deserialization
    }

    public ReviewResponse(Long id, Long bookingId, Long itemId, String itemName, 
                         Long renterId, String renterName, Integer score, 
                         String comment, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.renterId = renterId;
        this.renterName = renterName;
        this.score = score;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Long getRenterId() {
        return renterId;
    }

    public void setRenterId(Long renterId) {
        this.renterId = renterId;
    }

    public String getRenterName() {
        return renterName;
    }

    public void setRenterName(String renterName) {
        this.renterName = renterName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
