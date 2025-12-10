package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.dto.RenterReviewResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RenterReviewResponseTest {

    @Test
    void testDefaultConstructor() {
        RenterReviewResponse response = new RenterReviewResponse();
        
        assertNull(response.getId());
        assertNull(response.getBookingId());
        assertNull(response.getRenterId());
        assertNull(response.getRenterName());
        assertNull(response.getItemId());
        assertNull(response.getItemName());
        assertNull(response.getOwnerId());
        assertNull(response.getScore());
        assertNull(response.getComment());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        RenterReviewResponse response = new RenterReviewResponse(
            1L, 2L, 3L, "johndoe", 4L, "Guitar", 5L, 5, "Great renter!", now
        );
        
        assertEquals(1L, response.getId());
        assertEquals(2L, response.getBookingId());
        assertEquals(3L, response.getRenterId());
        assertEquals("johndoe", response.getRenterName());
        assertEquals(4L, response.getItemId());
        assertEquals("Guitar", response.getItemName());
        assertEquals(5L, response.getOwnerId());
        assertEquals(5, response.getScore());
        assertEquals("Great renter!", response.getComment());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        RenterReviewResponse response = new RenterReviewResponse();
        LocalDateTime now = LocalDateTime.now();
        
        response.setId(10L);
        response.setBookingId(20L);
        response.setRenterId(30L);
        response.setRenterName("janedoe");
        response.setItemId(40L);
        response.setItemName("Piano");
        response.setOwnerId(50L);
        response.setScore(4);
        response.setComment("Good experience");
        response.setCreatedAt(now);
        
        assertEquals(10L, response.getId());
        assertEquals(20L, response.getBookingId());
        assertEquals(30L, response.getRenterId());
        assertEquals("janedoe", response.getRenterName());
        assertEquals(40L, response.getItemId());
        assertEquals("Piano", response.getItemName());
        assertEquals(50L, response.getOwnerId());
        assertEquals(4, response.getScore());
        assertEquals("Good experience", response.getComment());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    void testSetNullValues() {
        RenterReviewResponse response = new RenterReviewResponse(
            1L, 2L, 3L, "johndoe", 4L, "Guitar", 5L, 5, "Great!", LocalDateTime.now()
        );
        
        response.setId(null);
        response.setBookingId(null);
        response.setRenterId(null);
        response.setRenterName(null);
        response.setItemId(null);
        response.setItemName(null);
        response.setOwnerId(null);
        response.setScore(null);
        response.setComment(null);
        response.setCreatedAt(null);
        
        assertNull(response.getId());
        assertNull(response.getBookingId());
        assertNull(response.getRenterId());
        assertNull(response.getRenterName());
        assertNull(response.getItemId());
        assertNull(response.getItemName());
        assertNull(response.getOwnerId());
        assertNull(response.getScore());
        assertNull(response.getComment());
        assertNull(response.getCreatedAt());
    }
}
