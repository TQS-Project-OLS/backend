package com.example.OLSHEETS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Item;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking b where b.item.id = :itemId and b.startDate <= :endDate and b.endDate >= :startDate")
    List<Booking> findOverlapping(@Param("itemId") Long itemId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Admin oversight query methods
    List<Booking> findByStatus(BookingStatus status);
    
    List<Booking> findByRenterId(Long renterId);
    
    Long countByStatus(BookingStatus status);
    
    Long countByRenterId(Long renterId);
    
    Long countByItemIn(List<Item> items);
    
    List<Booking> findByItemInAndStatus(List<Item> items, BookingStatus status);

}

