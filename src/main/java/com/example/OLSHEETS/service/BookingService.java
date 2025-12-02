package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Booking createBooking(Long instrumentId, Long ownerId, Long renterId, LocalDate startDate, LocalDate endDate){
        if(startDate == null || endDate == null || !startDate.isBefore(endDate) && !startDate.isEqual(endDate)){
            throw new IllegalArgumentException("Invalid dates");
        }

        List<Booking> overlapping = bookingRepository.findOverlapping(instrumentId, startDate, endDate);
        if(!overlapping.isEmpty()){
            throw new IllegalStateException("Instrument already booked for requested period");
        }

        Booking b = new Booking(instrumentId, ownerId, renterId, startDate, endDate);
        return bookingRepository.save(b);
    }

    @Transactional
    public Booking approveBooking(Long bookingId, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        if (!booking.getOwnerId().equals(ownerId)) {
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
    public Booking rejectBooking(Long bookingId, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        if (!booking.getOwnerId().equals(ownerId)) {
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

