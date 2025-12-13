package com.example.OLSHEETS.data;

import jakarta.persistence.Entity;

/**
 * RenterReview entity for owners rating renters
 * after a booking has been completed.
 */
@Entity
public class RenterReview extends BaseReview {

    public RenterReview() {
        super();
    }

    public RenterReview(Booking booking, Integer score, String comment) {
        super(booking, score, comment);
    }
}
