package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Booking createBooking(Long instrumentId, LocalDate startDate, LocalDate endDate){
        if(startDate == null || endDate == null || !startDate.isBefore(endDate) && !startDate.isEqual(endDate)){
            throw new IllegalArgumentException("Invalid dates");
        }

        List<Booking> overlapping = bookingRepository.findOverlapping(instrumentId, startDate, endDate);
        if(!overlapping.isEmpty()){
            throw new IllegalStateException("Instrument already booked for requested period");
        }

        Booking b = new Booking(instrumentId, startDate, endDate);
        return bookingRepository.save(b);
    }

    public List<Booking> listBookings(){
        return bookingRepository.findAll();
    }
}

