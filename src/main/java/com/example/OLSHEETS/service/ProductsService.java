package com.example.OLSHEETS.service;

import com.example.OLSHEETS.entity.InstrumentEntity;
import com.example.OLSHEETS.mapper.InstrumentMapper;
import com.example.OLSHEETS.model.Instrument;
import com.example.OLSHEETS.repository.InstrumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsService {

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private InstrumentMapper instrumentMapper;

    public List<Instrument> searchInstrumentsByName(String name) {
        List<InstrumentEntity> entities = instrumentRepository.findByNameContainingIgnoreCase(name);
        return instrumentMapper.toModelList(entities);
    }
}
