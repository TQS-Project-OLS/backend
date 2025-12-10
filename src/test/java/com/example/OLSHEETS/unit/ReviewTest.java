package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.Review;
import com.example.OLSHEETS.data.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    private Booking booking;
    private User renter;
    private User owner;
    private Instrument item;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "owner@example.com", "Owner User", "password");
        owner.setId(1L);

        renter = new User("renter", "renter@example.com", "Renter User", "password");
        renter.setId(2L);

        item = new Instrument();
        item.setId(10L);
        item.setName("Test Guitar");
        item.setOwner(owner);

        booking = new Booking(item, renter, LocalDate.now(), LocalDate.now().plusDays(1));
        booking.setId(100L);
    }

    @Test
    void testDefaultConstructor() {
        Review review = new Review();
        
        assertNull(review.getId());
        assertNull(review.getBooking());
        assertNull(review.getScore());
        assertNull(review.getComment());
        assertNotNull(review.getCreatedAt()); // Set in constructor
    }

    @Test
    void testParameterizedConstructor() {
        Review review = new Review(booking, 5, "Excellent instrument!");
        
        assertNull(review.getId());
        assertEquals(booking, review.getBooking());
        assertEquals(5, review.getScore());
        assertEquals("Excellent instrument!", review.getComment());
        assertNotNull(review.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        Review review = new Review();
        
        review.setId(1L);
        review.setBooking(booking);
        review.setScore(4);
        review.setComment("Good quality");
        
        assertEquals(1L, review.getId());
        assertEquals(booking, review.getBooking());
        assertEquals(4, review.getScore());
        assertEquals("Good quality", review.getComment());
    }

    @Test
    void testScoreValidation_WithValidScore() {
        Review review = new Review(booking, 5, "Great!");
        assertEquals(5, review.getScore());
        
        review.setScore(1);
        assertEquals(1, review.getScore());
        
        review.setScore(3);
        assertEquals(3, review.getScore());
    }

    @Test
    void testCommentCanBeNull() {
        Review review = new Review(booking, 4, null);
        assertNull(review.getComment());
    }

    @Test
    void testCommentCanBeEmpty() {
        Review review = new Review(booking, 4, "");
        assertEquals("", review.getComment());
    }

    @Test
    void testCreatedAtIsSetAutomatically() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Review review = new Review(booking, 5, "Great!");
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        assertNotNull(review.getCreatedAt());
        assertTrue(review.getCreatedAt().isAfter(before));
        assertTrue(review.getCreatedAt().isBefore(after));
    }
}
