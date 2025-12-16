package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.repository.ItemRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.OLSHEETS.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired(required = false)
    private Counter bookingsCreatedCounter;

    @Autowired(required = false)
    private Counter bookingsApprovedCounter;

    @Autowired(required = false)
    private Counter bookingsRejectedCounter;

    @Autowired(required = false)
    private Timer bookingCreationTimer;

    public BookingService(BookingRepository bookingRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Booking createBooking(Long itemId, Long renterId, LocalDate startDate, LocalDate endDate){
        java.util.function.Supplier<Booking> bookingSupplier = () -> {
            // Validate dates are not null
            if (startDate == null) {
                throw new IllegalArgumentException("Start date cannot be null");
            }
            if (endDate == null) {
                throw new IllegalArgumentException("End date cannot be null");
            }

            // Validate start date is not in the past
            if (startDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Cannot book items for past dates");
            }

            // Validate end date is after start date
            if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
                throw new IllegalArgumentException("End date must be after start date");
            }

            Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + itemId));

            User renter = userRepository.findById(renterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + renterId));

            // Validate renter is not the owner of the item
            if (item.getOwner().getId().equals(renterId)) {
                throw new IllegalArgumentException("Cannot book your own items");
            }

            List<Booking> overlapping = bookingRepository.findOverlapping(itemId, startDate, endDate);
            if(!overlapping.isEmpty()){
                throw new IllegalStateException("Item already booked for requested period");
            }

            Booking b = new Booking(item, renter, startDate, endDate);
            return bookingRepository.save(b);
        };

        if (bookingCreationTimer != null) {
            return bookingCreationTimer.record(bookingSupplier);
        } else {
            return bookingSupplier.get();
        }

    }

    @Transactional
    public Booking approveBooking(Long bookingId, int ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        if (!booking.getItem().getOwner().getId().equals(Long.valueOf(ownerId))) {
            throw new IllegalArgumentException("You are not authorized to approve this booking");
        }

        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new IllegalStateException("Booking has already been approved");
        }

        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new IllegalStateException("Cannot approve a rejected booking");
        }

        // Check for overlapping APPROVED bookings to prevent double-booking
        List<Booking> overlappingBookings = bookingRepository.findOverlapping(
            booking.getItem().getId(),
            booking.getStartDate(),
            booking.getEndDate()
        );
        
        boolean hasApprovedOverlap = overlappingBookings.stream()
            .anyMatch(b -> b.getStatus() == BookingStatus.APPROVED && !b.getId().equals(bookingId));
        
        if (hasApprovedOverlap) {
            throw new IllegalStateException("Cannot approve: conflicts with already approved booking");
        }

        booking.setStatus(BookingStatus.APPROVED);
        Booking saved = bookingRepository.save(booking);
        if (bookingsApprovedCounter != null) {
            bookingsApprovedCounter.increment();
        }
        return saved;
    }

    @Transactional
    public Booking rejectBooking(Long bookingId, int ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        if (!booking.getItem().getOwner().getId().equals(Long.valueOf(ownerId))) {
            throw new IllegalArgumentException("You are not authorized to reject this booking");
        }

        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new IllegalStateException("Booking has already been rejected");
        }

        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new IllegalStateException("Cannot reject an approved booking");
        }

        booking.setStatus(BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);
        if (bookingsRejectedCounter != null) {
            bookingsRejectedCounter.increment();
        }
        return saved;
    }

    public List<Booking> listBookings(){
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByRenterId(Long renterId) {
        return bookingRepository.findByRenterId(renterId);
    }
}
