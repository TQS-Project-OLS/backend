package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.Availability;
import com.example.OLSHEETS.data.AvailabilityReason;
import com.example.OLSHEETS.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public Availability createUnavailability(
            @RequestParam Long instrumentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam AvailabilityReason reason) {
        return availabilityService.createUnavailability(instrumentId, startDate, endDate, reason);
    }

    @GetMapping
    public List<Availability> listAvailabilities() {
        return availabilityService.listAvailabilities();
    }

    @GetMapping("/instrument/{instrumentId}")
    public List<Availability> getInstrumentAvailabilities(@PathVariable Long instrumentId) {
        return availabilityService.getInstrumentAvailabilities(instrumentId);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam Long instrumentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        boolean isAvailable = availabilityService.isAvailable(instrumentId, startDate, endDate);
        return ResponseEntity.ok(isAvailable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnavailability(@PathVariable Long id) {
        availabilityService.deleteUnavailability(id);
        return ResponseEntity.noContent().build();
    }
}
