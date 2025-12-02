package com.example.OLSHEETS.repository;

import com.example.OLSHEETS.data.MusicSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicSheetRepository extends JpaRepository<MusicSheet, Long> {
    List<MusicSheet> findByCategory(String category);
    List<MusicSheet> findByTitleContainingIgnoreCase(String title);
    List<MusicSheet> findByAvailable(boolean available);
}

