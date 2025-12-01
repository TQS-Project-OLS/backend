package com.example.OLSHEETS.repository;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicSheetRepository extends JpaRepository<MusicSheet, Long> {

    List<MusicSheet> findByNameContainingIgnoreCase(String name);

    List<MusicSheet> findByCategory(SheetCategory category);
}
