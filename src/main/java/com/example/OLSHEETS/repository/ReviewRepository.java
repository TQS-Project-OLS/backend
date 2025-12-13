package com.example.OLSHEETS.repository;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * Check if a review already exists for a booking
     */
    boolean existsByBooking(Booking booking);
    
    /**
     * Find a review by booking
     */
    Optional<Review> findByBooking(Booking booking);
    
    /**
     * Find all reviews for a specific item (instrument or music sheet)
     */
    @Query("SELECT r FROM Review r WHERE r.booking.item.id = :itemId")
    List<Review> findByItemId(@Param("itemId") Long itemId);
    
    /**
     * Get average score for an item
     */
    @Query("SELECT AVG(r.score) FROM Review r WHERE r.booking.item.id = :itemId")
    Double getAverageScoreByItemId(@Param("itemId") Long itemId);
}
