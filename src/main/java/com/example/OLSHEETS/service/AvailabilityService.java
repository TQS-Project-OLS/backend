package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.Availability;
import com.example.OLSHEETS.data.AvailabilityRepository;
import com.example.OLSHEETS.data.AvailabilityReason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional
    public Availability createUnavailability(Long instrumentId, LocalDate startDate, LocalDate endDate, AvailabilityReason reason) {
        if (startDate == null || endDate == null || !startDate.isBefore(endDate) && !startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("Invalid dates");
        }

        Availability availability = new Availability(instrumentId, startDate, endDate, reason);
        return availabilityRepository.save(availability);
    }

    public List<Availability> listAvailabilities() {
        return availabilityRepository.findAll();
    }

    public List<Availability> getInstrumentAvailabilities(Long instrumentId) {
        return availabilityRepository.findByInstrumentId(instrumentId);
    }

    public boolean isAvailable(Long instrumentId, LocalDate startDate, LocalDate endDate) {
        List<Availability> overlapping = availabilityRepository.findOverlapping(instrumentId, startDate, endDate);
        return overlapping.isEmpty();
    }

    @Transactional
    public void deleteUnavailability(Long availabilityId) {
        availabilityRepository.deleteById(availabilityId);
    }
}
