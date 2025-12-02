package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;

    public AdminService(BookingRepository bookingRepository, ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Get all bookings in the system
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Filter bookings by status
     */
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    /**
     * Get all bookings for a specific renter
     */
    public List<Booking> getBookingsByRenter(Long renterId) {
        return bookingRepository.findByRenterId(renterId);
    }

    /**
     * Cancel a booking as admin (override owner permissions)
     */
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    /**
     * Get booking statistics (counts by status)
     */
    public Map<String, Long> getBookingStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", bookingRepository.count());
        stats.put("pending", bookingRepository.countByStatus(BookingStatus.PENDING));
        stats.put("approved", bookingRepository.countByStatus(BookingStatus.APPROVED));
        stats.put("rejected", bookingRepository.countByStatus(BookingStatus.REJECTED));
        stats.put("cancelled", bookingRepository.countByStatus(BookingStatus.CANCELLED));
        return stats;
    }

    /**
     * Get activity count for a specific renter
     */
    public Long getRenterActivity(Long renterId) {
        return bookingRepository.countByRenterId(renterId);
    }

    /**
     * Get activity count for a specific owner (bookings on their items)
     */
    public Long getOwnerActivity(int ownerId) {
        List<Item> ownerItems = itemRepository.findByOwnerId(ownerId);
        return bookingRepository.countByItemIn(ownerItems);
    }

    /**
     * Calculate total revenue for a specific owner (approved bookings only)
     */
    public Double getRevenueByOwner(int ownerId) {
        List<Item> ownerItems = itemRepository.findByOwnerId(ownerId);
        List<Booking> approvedBookings = bookingRepository.findByItemInAndStatus(ownerItems, BookingStatus.APPROVED);
        
        return calculateRevenue(approvedBookings);
    }

    /**
     * Calculate total system revenue (all approved bookings)
     */
    public Double getTotalRevenue() {
        List<Booking> approvedBookings = bookingRepository.findByStatus(BookingStatus.APPROVED);
        return calculateRevenue(approvedBookings);
    }

    /**
     * Helper method to calculate revenue from a list of bookings
     * Revenue = sum of (price per day * number of days) for each booking
     */
    private Double calculateRevenue(List<Booking> bookings) {
        double totalRevenue = 0.0;
        for (Booking booking : bookings) {
            long days = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
            double bookingRevenue = booking.getItem().getPrice() * days;
            totalRevenue += bookingRevenue;
        }
        return totalRevenue;
    }
}