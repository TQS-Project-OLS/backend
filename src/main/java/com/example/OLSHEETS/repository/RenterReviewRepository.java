package com.example.OLSHEETS.repository;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.RenterReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RenterReviewRepository extends JpaRepository<RenterReview, Long> {
    
    /**
     * Check if a renter review already exists for a booking
     */
    boolean existsByBooking(Booking booking);
    
    /**
     * Find a renter review by booking
     */
    Optional<RenterReview> findByBooking(Booking booking);
    
    /**
     * Find all reviews for a specific renter (by renter ID)
     */
    @Query("SELECT r FROM RenterReview r WHERE r.booking.renter.id = :renterId")
    List<RenterReview> findByRenterId(@Param("renterId") Long renterId);
    
    /**
     * Get average score for a renter
     */
    @Query("SELECT AVG(r.score) FROM RenterReview r WHERE r.booking.renter.id = :renterId")
    Double getAverageScoreByRenterId(@Param("renterId") Long renterId);
}
