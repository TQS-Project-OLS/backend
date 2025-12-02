package com.example.OLSHEETS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.OLSHEETS.data.Availability;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    @Query("select a from Availability a where a.instrumentId = :instrumentId and a.startDate <= :endDate and a.endDate >= :startDate")
    List<Availability> findOverlapping(@Param("instrumentId") Long instrumentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Availability> findByInstrumentId(Long instrumentId);
}
