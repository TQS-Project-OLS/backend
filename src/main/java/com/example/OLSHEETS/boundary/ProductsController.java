package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.dto.InstrumentRegistrationRequest;
import com.example.OLSHEETS.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class ProductsController {

    @Autowired
    private ProductsService productsService;

    @GetMapping("/search")
    public ResponseEntity<List<Instrument>> searchInstruments(@RequestParam String name) {
        List<Instrument> instruments = productsService.searchInstrumentsByName(name);
        return ResponseEntity.ok(instruments);
    }

    @GetMapping("/filter/type")
    public ResponseEntity<List<Instrument>> filterByType(@RequestParam InstrumentType type) {
        List<Instrument> instruments = productsService.filterInstrumentsByType(type);
        return ResponseEntity.ok(instruments);

    }
    @GetMapping("/filter/family")
    public ResponseEntity<List<Instrument>> filterByFamily(@RequestParam InstrumentFamily family) {
        List<Instrument> instruments = productsService.filterInstrumentsByFamily(family);
        return ResponseEntity.ok(instruments);
    }
    
    @PostMapping("/register")
    public ResponseEntity<Instrument> registerInstrument(@RequestBody InstrumentRegistrationRequest request) {
        Instrument registeredInstrument = productsService.registerInstrument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredInstrument);
    }
}
