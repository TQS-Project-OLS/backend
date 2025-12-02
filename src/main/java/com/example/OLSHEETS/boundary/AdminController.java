package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Get all bookings in the system
     */
    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        return adminService.getAllBookings();
    }

    /**
     * Get bookings filtered by status
     */
    @GetMapping("/bookings/status/{status}")
    public List<Booking> getBookingsByStatus(@PathVariable String status) {
        BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
        return adminService.getBookingsByStatus(bookingStatus);
    }

    /**
     * Get all bookings for a specific renter
     */
    @GetMapping("/bookings/renter/{renterId}")
    public List<Booking> getBookingsByRenter(@PathVariable Long renterId) {
        return adminService.getBookingsByRenter(renterId);
    }

    /**
     * Cancel a booking (admin override)
     */
    @PutMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            Booking cancelled = adminService.cancelBooking(bookingId);
            return ResponseEntity.ok(cancelled);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get booking statistics
     */
    @GetMapping("/statistics/bookings")
    public Map<String, Long> getBookingStatistics() {
        return adminService.getBookingStatistics();
    }

    /**
     * Get activity for a specific renter
     */
    @GetMapping("/activity/renter/{renterId}")
    public Map<String, Object> getRenterActivity(@PathVariable Long renterId) {
        Long bookingCount = adminService.getRenterActivity(renterId);
        Map<String, Object> response = new HashMap<>();
        response.put("renterId", renterId);
        response.put("bookingCount", bookingCount);
        return response;
    }

    /**
     * Get activity for a specific owner
     */
    @GetMapping("/activity/owner/{ownerId}")
    public Map<String, Object> getOwnerActivity(@PathVariable int ownerId) {
        Long bookingCount = adminService.getOwnerActivity(ownerId);
        Map<String, Object> response = new HashMap<>();
        response.put("ownerId", ownerId);
        response.put("bookingCount", bookingCount);
        return response;
    }

    /**
     * Get revenue for a specific owner
     */
    @GetMapping("/revenue/owner/{ownerId}")
    public Map<String, Object> getRevenueByOwner(@PathVariable int ownerId) {
        Double revenue = adminService.getRevenueByOwner(ownerId);
        Map<String, Object> response = new HashMap<>();
        response.put("ownerId", ownerId);
        response.put("revenue", revenue);
        return response;
    }

    /**
     * Get total system revenue
     */
    @GetMapping("/revenue/total")
    public Map<String, Double> getTotalRevenue() {
        Double totalRevenue = adminService.getTotalRevenue();
        Map<String, Double> response = new HashMap<>();
        response.put("totalRevenue", totalRevenue);
        return response;
    }
}