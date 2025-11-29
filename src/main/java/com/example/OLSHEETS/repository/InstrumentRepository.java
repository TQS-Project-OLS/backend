package com.example.OLSHEETS.repository;

import com.example.OLSHEETS.entity.InstrumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstrumentRepository extends JpaRepository<InstrumentEntity, Long> {

    List<InstrumentEntity> findByNameContainingIgnoreCase(String name);
}
