package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.SheetBooking;
import com.example.OLSHEETS.service.SheetBookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sheets/bookings")
public class SheetBookingController {

    private final SheetBookingService bookingService;

    public SheetBookingController(SheetBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> request) {
        try {
            Long sheetId = Long.valueOf(request.get("sheetId").toString());
            Long renterId = Long.valueOf(request.get("renterId").toString());
            LocalDate startDate = LocalDate.parse(request.get("startDate").toString());
            LocalDate endDate = LocalDate.parse(request.get("endDate").toString());

            SheetBooking booking = bookingService.createBooking(sheetId, renterId, startDate, endDate);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/renter/{renterId}")
    public ResponseEntity<List<SheetBooking>> getBookingsByRenter(@PathVariable Long renterId) {
        List<SheetBooking> bookings = bookingService.getBookingsByRenter(renterId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<SheetBooking> getBookingById(@PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<SheetBooking>> getAllBookings() {
        List<SheetBooking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/sheet/{sheetId}")
    public ResponseEntity<List<SheetBooking>> getBookingsBySheet(@PathVariable Long sheetId) {
        try {
            List<SheetBooking> bookings = bookingService.getBookingsBySheet(sheetId);
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

