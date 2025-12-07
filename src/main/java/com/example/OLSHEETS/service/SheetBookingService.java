package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetBooking;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SheetBookingService {

    private final SheetBookingRepository bookingRepository;
    private final MusicSheetRepository sheetRepository;
    private final com.example.OLSHEETS.repository.UserRepository userRepository;

    public SheetBookingService(SheetBookingRepository bookingRepository, MusicSheetRepository sheetRepository, com.example.OLSHEETS.repository.UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.sheetRepository = sheetRepository;
        this.userRepository = userRepository;
    }

    public SheetBooking createBooking(Long sheetId, Long renterId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        MusicSheet sheet = sheetRepository.findById(sheetId)
            .orElseThrow(() -> new IllegalArgumentException("Music sheet not found with id: " + sheetId));

        List<SheetBooking> conflicts = bookingRepository.findConflictingBookings(sheetId, startDate, endDate);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Music sheet is already booked for the selected dates");
        }

        User renter = userRepository.findById(renterId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + renterId));

        SheetBooking booking = new SheetBooking(sheet, renter, startDate, endDate);
        return bookingRepository.save(booking);
    }

    public List<SheetBooking> getBookingsByRenter(Long renterId) {
        return bookingRepository.findByRenterId(renterId);
    }

    public Optional<SheetBooking> getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public List<SheetBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<SheetBooking> getBookingsBySheet(Long sheetId) {
        MusicSheet sheet = sheetRepository.findById(sheetId)
            .orElseThrow(() -> new IllegalArgumentException("Music sheet not found with id: " + sheetId));
        return bookingRepository.findByMusicSheet(sheet);
    }
}

