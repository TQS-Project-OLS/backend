package com.example.OLSHEETS.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking b where b.instrumentId = :instrumentId and b.startDate <= :endDate and b.endDate >= :startDate")
    List<Booking> findOverlapping(@Param("instrumentId") Long instrumentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}

