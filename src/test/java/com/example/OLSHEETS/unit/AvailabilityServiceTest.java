package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Availability;
import com.example.OLSHEETS.data.AvailabilityReason;
import com.example.OLSHEETS.repository.AvailabilityRepository;
import com.example.OLSHEETS.service.AvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Availability availability1;
    private Availability availability2;

    @BeforeEach
    void setUp() {
        availability1 = new Availability(1L, LocalDate.of(2025, 2, 11), LocalDate.of(2025, 2, 14), AvailabilityReason.OWNER_USE);
        availability2 = new Availability(1L, LocalDate.of(2025, 2, 16), LocalDate.of(2025, 2, 19), AvailabilityReason.MAINTENANCE);
    }

    @Test
    @Requirement("OLS-33")
    void testCreateUnavailability_WithValidDates_ShouldCreateAndReturn() {
        Long instrumentId = 1L;
        LocalDate startDate = LocalDate.of(2025, 3, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 5);
        AvailabilityReason reason = AvailabilityReason.MAINTENANCE;

        Availability expected = new Availability(instrumentId, startDate, endDate, reason);
        when(availabilityRepository.save(any(Availability.class))).thenReturn(expected);

        Availability result = availabilityService.createUnavailability(instrumentId, startDate, endDate, reason);

        assertThat(result).isNotNull();
        assertThat(result.getInstrumentId()).isEqualTo(instrumentId);
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getReason()).isEqualTo(reason);
        verify(availabilityRepository, times(1)).save(any(Availability.class));
    }

    @Test
    @Requirement("OLS-33")
    void testCreateUnavailability_WithNullStartDate_ShouldThrowException() {
        assertThatThrownBy(() ->
                availabilityService.createUnavailability(1L, null, LocalDate.of(2025, 3, 5), AvailabilityReason.OWNER_USE)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Invalid dates");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @Requirement("OLS-33")
    void testCreateUnavailability_WithNullEndDate_ShouldThrowException() {
        assertThatThrownBy(() ->
                availabilityService.createUnavailability(1L, LocalDate.of(2025, 3, 1), null, AvailabilityReason.OWNER_USE)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Invalid dates");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @Requirement("OLS-33")
    void testCreateUnavailability_WithStartDateAfterEndDate_ShouldThrowException() {
        assertThatThrownBy(() ->
                availabilityService.createUnavailability(1L, LocalDate.of(2025, 3, 10), LocalDate.of(2025, 3, 5), AvailabilityReason.OWNER_USE)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Invalid dates");

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    @Requirement("OLS-33")
    void testCreateUnavailability_WithEqualDates_ShouldSucceed() {
        LocalDate sameDate = LocalDate.of(2025, 3, 15);
        Availability expected = new Availability(1L, sameDate, sameDate, AvailabilityReason.MAINTENANCE);
        when(availabilityRepository.save(any(Availability.class))).thenReturn(expected);

        Availability result = availabilityService.createUnavailability(1L, sameDate, sameDate, AvailabilityReason.MAINTENANCE);

        assertThat(result).isNotNull();
        verify(availabilityRepository, times(1)).save(any(Availability.class));
    }

    @Test
    @Requirement("OLS-33")
    void testListAvailabilities_ShouldReturnAllAvailabilities() {
        List<Availability> expected = Arrays.asList(availability1, availability2);
        when(availabilityRepository.findAll()).thenReturn(expected);

        List<Availability> result = availabilityService.listAvailabilities();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(availability1, availability2);
        verify(availabilityRepository, times(1)).findAll();
    }

    @Test
    @Requirement("OLS-33")
    void testListAvailabilities_WithNoData_ShouldReturnEmptyList() {
        when(availabilityRepository.findAll()).thenReturn(Collections.emptyList());

        List<Availability> result = availabilityService.listAvailabilities();

        assertThat(result).isEmpty();
        verify(availabilityRepository, times(1)).findAll();
    }

    @Test
    @Requirement("OLS-33")
    void testGetInstrumentAvailabilities_ShouldReturnAvailabilitiesForInstrument() {
        Long instrumentId = 1L;
        List<Availability> expected = Arrays.asList(availability1, availability2);
        when(availabilityRepository.findByInstrumentId(instrumentId)).thenReturn(expected);

        List<Availability> result = availabilityService.getInstrumentAvailabilities(instrumentId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(availability1, availability2);
        verify(availabilityRepository, times(1)).findByInstrumentId(instrumentId);
    }

    @Test
    @Requirement("OLS-33")
    void testGetInstrumentAvailabilities_WithNoAvailabilities_ShouldReturnEmptyList() {
        Long instrumentId = 99L;
        when(availabilityRepository.findByInstrumentId(instrumentId)).thenReturn(Collections.emptyList());

        List<Availability> result = availabilityService.getInstrumentAvailabilities(instrumentId);

        assertThat(result).isEmpty();
        verify(availabilityRepository, times(1)).findByInstrumentId(instrumentId);
    }

    @Test
    @Requirement("OLS-33")
    void testIsAvailable_WithNoOverlap_ShouldReturnTrue() {
        Long instrumentId = 1L;
        LocalDate startDate = LocalDate.of(2025, 3, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 5);

        when(availabilityRepository.findOverlapping(instrumentId, startDate, endDate)).thenReturn(Collections.emptyList());

        boolean result = availabilityService.isAvailable(instrumentId, startDate, endDate);

        assertThat(result).isTrue();
        verify(availabilityRepository, times(1)).findOverlapping(instrumentId, startDate, endDate);
    }

    @Test
    @Requirement("OLS-33")
    void testIsAvailable_WithOverlap_ShouldReturnFalse() {
        Long instrumentId = 1L;
        LocalDate startDate = LocalDate.of(2025, 2, 12);
        LocalDate endDate = LocalDate.of(2025, 2, 13);

        when(availabilityRepository.findOverlapping(instrumentId, startDate, endDate))
                .thenReturn(Collections.singletonList(availability1));

        boolean result = availabilityService.isAvailable(instrumentId, startDate, endDate);

        assertThat(result).isFalse();
        verify(availabilityRepository, times(1)).findOverlapping(instrumentId, startDate, endDate);
    }

    @Test
    @Requirement("OLS-33")
    void testIsAvailable_WithMultipleOverlaps_ShouldReturnFalse() {
        Long instrumentId = 1L;
        LocalDate startDate = LocalDate.of(2025, 2, 10);
        LocalDate endDate = LocalDate.of(2025, 2, 20);

        when(availabilityRepository.findOverlapping(instrumentId, startDate, endDate))
                .thenReturn(Arrays.asList(availability1, availability2));

        boolean result = availabilityService.isAvailable(instrumentId, startDate, endDate);

        assertThat(result).isFalse();
        verify(availabilityRepository, times(1)).findOverlapping(instrumentId, startDate, endDate);
    }

    @Test
    @Requirement("OLS-33")
    void testDeleteUnavailability_ShouldCallRepository() {
        Long availabilityId = 1L;

        availabilityService.deleteUnavailability(availabilityId);

        verify(availabilityRepository, times(1)).deleteById(availabilityId);
    }

    @Test
    @Requirement("OLS-33")
    void testCreateUnavailability_WithOwnerUseReason_ShouldSucceed() {
        Availability expected = new Availability(5L, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 10), AvailabilityReason.OWNER_USE);
        when(availabilityRepository.save(any(Availability.class))).thenReturn(expected);

        Availability result = availabilityService.createUnavailability(5L, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 10), AvailabilityReason.OWNER_USE);

        assertThat(result.getReason()).isEqualTo(AvailabilityReason.OWNER_USE);
        verify(availabilityRepository, times(1)).save(any(Availability.class));
    }

    @Test
    @Requirement("OLS-33")
    void testCreateUnavailability_WithMaintenanceReason_ShouldSucceed() {
        Availability expected = new Availability(3L, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 5), AvailabilityReason.MAINTENANCE);
        when(availabilityRepository.save(any(Availability.class))).thenReturn(expected);

        Availability result = availabilityService.createUnavailability(3L, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 5), AvailabilityReason.MAINTENANCE);

        assertThat(result.getReason()).isEqualTo(AvailabilityReason.MAINTENANCE);
        verify(availabilityRepository, times(1)).save(any(Availability.class));
    }
}

