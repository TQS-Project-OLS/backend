package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Item;
import com.example.OLSHEETS.repository.ItemRepository;
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

    public BookingService(BookingRepository bookingRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Booking createBooking(Long itemId, Long renterId, LocalDate startDate, LocalDate endDate){
        if(startDate == null || endDate == null || !startDate.isBefore(endDate) && !startDate.isEqual(endDate)){
            throw new IllegalArgumentException("Invalid dates");
        }

        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + itemId));

        List<Booking> overlapping = bookingRepository.findOverlapping(itemId, startDate, endDate);
        if(!overlapping.isEmpty()){
            throw new IllegalStateException("Item already booked for requested period");
        }

        User renter = userRepository.findById(renterId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + renterId));

        Booking b = new Booking(item, renter, startDate, endDate);
        return bookingRepository.save(b);
    }

    @Transactional
    public Booking approveBooking(Long bookingId, int ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        if (booking.getItem().getOwner().getId() != ownerId) {
            throw new IllegalArgumentException("You are not authorized to approve this booking");
        }

        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new IllegalStateException("Booking has already been approved");
        }

        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new IllegalStateException("Cannot approve a rejected booking");
        }

        booking.setStatus(BookingStatus.APPROVED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking rejectBooking(Long bookingId, int ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        if (booking.getItem().getOwner().getId() != ownerId) {
            throw new IllegalArgumentException("You are not authorized to reject this booking");
        }

        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new IllegalStateException("Booking has already been rejected");
        }

        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new IllegalStateException("Cannot reject an approved booking");
        }

        booking.setStatus(BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    public List<Booking> listBookings(){
        return bookingRepository.findAll();
    }
}