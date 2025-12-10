package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.dto.ReviewResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReviewResponseTest {

    @Test
    void testDefaultConstructor() {
        ReviewResponse response = new ReviewResponse();
        
        assertNull(response.getId());
        assertNull(response.getBookingId());
        assertNull(response.getItemId());
        assertNull(response.getItemName());
        assertNull(response.getRenterId());
        assertNull(response.getRenterName());
        assertNull(response.getScore());
        assertNull(response.getComment());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ReviewResponse response = new ReviewResponse(
            1L, 2L, 3L, "Guitar", 4L, "johndoe", 5, "Great instrument!", now
        );
        
        assertEquals(1L, response.getId());
        assertEquals(2L, response.getBookingId());
        assertEquals(3L, response.getItemId());
        assertEquals("Guitar", response.getItemName());
        assertEquals(4L, response.getRenterId());
        assertEquals("johndoe", response.getRenterName());
        assertEquals(5, response.getScore());
        assertEquals("Great instrument!", response.getComment());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        ReviewResponse response = new ReviewResponse();
        LocalDateTime now = LocalDateTime.now();
        
        response.setId(10L);
        response.setBookingId(20L);
        response.setItemId(30L);
        response.setItemName("Piano");
        response.setRenterId(40L);
        response.setRenterName("janedoe");
        response.setScore(4);
        response.setComment("Good quality");
        response.setCreatedAt(now);
        
        assertEquals(10L, response.getId());
        assertEquals(20L, response.getBookingId());
        assertEquals(30L, response.getItemId());
        assertEquals("Piano", response.getItemName());
        assertEquals(40L, response.getRenterId());
        assertEquals("janedoe", response.getRenterName());
        assertEquals(4, response.getScore());
        assertEquals("Good quality", response.getComment());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    void testSetNullValues() {
        ReviewResponse response = new ReviewResponse(
            1L, 2L, 3L, "Guitar", 4L, "johndoe", 5, "Great!", LocalDateTime.now()
        );
        
        response.setId(null);
        response.setBookingId(null);
        response.setItemId(null);
        response.setItemName(null);
        response.setRenterId(null);
        response.setRenterName(null);
        response.setScore(null);
        response.setComment(null);
        response.setCreatedAt(null);
        
        assertNull(response.getId());
        assertNull(response.getBookingId());
        assertNull(response.getItemId());
        assertNull(response.getItemName());
        assertNull(response.getRenterId());
        assertNull(response.getRenterName());
        assertNull(response.getScore());
        assertNull(response.getComment());
        assertNull(response.getCreatedAt());
    }
}
