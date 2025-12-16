package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.exception.UserNotFoundException;
import com.example.OLSHEETS.repository.UserRepository;
import com.example.OLSHEETS.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestParam Long itemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // Extract username from JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Get user from database
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Booking booking = bookingService.createBooking(itemId, user.getId(), startDate, endDate);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<Booking> listBookings() {
        return bookingService.listBookings();
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings() {
        try {
            // Extract username from JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Get user from database
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Get bookings where this user is the renter
            List<Booking> bookings = bookingService.getBookingsByRenterId(user.getId());
            return ResponseEntity.ok(bookings);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{bookingId}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable Long bookingId) {
        try {
            // Extract authenticated user from JWT token
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
            
            Booking approved = bookingService.approveBooking(bookingId, user.getId().intValue());
            return ResponseEntity.ok(approved);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{bookingId}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable Long bookingId) {
        try {
            // Extract authenticated user from JWT token
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
            
            Booking rejected = bookingService.rejectBooking(bookingId, user.getId().intValue());
            return ResponseEntity.ok(rejected);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }
}
