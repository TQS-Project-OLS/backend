package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.repository.InstrumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsService {

    @Autowired
    private InstrumentRepository instrumentRepository;

    public List<Instrument> searchInstrumentsByName(String name) {
        return instrumentRepository.findByNameContainingIgnoreCase(name);
    }
}
