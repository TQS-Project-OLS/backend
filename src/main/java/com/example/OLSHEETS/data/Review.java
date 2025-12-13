package com.example.OLSHEETS.data;

import jakarta.persistence.Entity;

/**
 * Review entity for renters rating items (instruments/music sheets)
 * after completing a booking.
 */
@Entity
public class Review extends BaseReview {

    public Review() {
        super();
    }

    public Review(Booking booking, Integer score, String comment) {
        super(booking, score, comment);
    }
}
