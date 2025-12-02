package com.example.OLSHEETS.repository;

import com.example.OLSHEETS.data.SheetBooking;
import com.example.OLSHEETS.data.MusicSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SheetBookingRepository extends JpaRepository<SheetBooking, Long> {
    List<SheetBooking> findByRenterId(Long renterId);
    List<SheetBooking> findByMusicSheet(MusicSheet musicSheet);

    @Query("SELECT sb FROM SheetBooking sb WHERE sb.musicSheet.id = :sheetId " +
           "AND sb.status != 'REJECTED' AND sb.status != 'CANCELLED' " +
           "AND ((sb.startDate <= :endDate) AND (sb.endDate >= :startDate))")
    List<SheetBooking> findConflictingBookings(
        @Param("sheetId") Long sheetId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

