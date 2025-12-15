package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.dto.ErrorResponse;
import com.example.OLSHEETS.dto.InstrumentRegistrationRequest;
import com.example.OLSHEETS.exception.UserNotFoundException;
import com.example.OLSHEETS.repository.UserRepository;
import com.example.OLSHEETS.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class ProductsController {

    @Autowired
    private ProductsService productsService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getInstrumentById(@PathVariable Long id) {
        try {
            Instrument instrument = productsService.getInstrumentById(id);
            return ResponseEntity.ok(instrument);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

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
    public ResponseEntity<Object> registerInstrument(@RequestBody InstrumentRegistrationRequest request) {
        try {
            // Extract username from JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Get user from database
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Set ownerId from authenticated user
            request.setOwnerId(user.getId());

            Instrument registeredInstrument = productsService.registerInstrument(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredInstrument);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
