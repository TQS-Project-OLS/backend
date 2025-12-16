package com.example.OLSHEETS.repository;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    List<Instrument> findByNameContainingIgnoreCase(String name);

    List<Instrument> findByType(InstrumentType type);
    
    List<Instrument> findByFamily(InstrumentFamily family);
    
    List<Instrument> findByOwnerId(Long ownerId);
}
