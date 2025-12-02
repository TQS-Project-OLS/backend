package com.example.OLSHEETS.repository;

import com.example.OLSHEETS.data.MusicSheet;
<<<<<<< HEAD
import com.example.OLSHEETS.data.SheetCategory;
=======
>>>>>>> origin/OLS-36-Book-music-sheets-for-available-date
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicSheetRepository extends JpaRepository<MusicSheet, Long> {
<<<<<<< HEAD

    List<MusicSheet> findByNameContainingIgnoreCase(String name);

    List<MusicSheet> findByCategory(SheetCategory category);
}
=======
    List<MusicSheet> findByCategory(String category);
    List<MusicSheet> findByTitleContainingIgnoreCase(String title);
    List<MusicSheet> findByAvailable(boolean available);
}

>>>>>>> origin/OLS-36-Book-music-sheets-for-available-date
