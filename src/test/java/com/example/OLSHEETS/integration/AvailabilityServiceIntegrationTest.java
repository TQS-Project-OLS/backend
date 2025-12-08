package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Availability;
import com.example.OLSHEETS.data.AvailabilityReason;
import com.example.OLSHEETS.repository.AvailabilityRepository;
import com.example.OLSHEETS.service.AvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
    "spring.main.lazy-initialization=true"
})
class AvailabilityServiceIntegrationTest {

    @Autowired
    AvailabilityService availabilityService;

    @Autowired
    AvailabilityRepository availabilityRepository;

    @BeforeEach
    void cleanup() {
        availabilityRepository.deleteAll();
    }

    @Test
    void shouldCreateAvailabilityWhenDatesAreValid() {
        Availability availability = availabilityService.createUnavailability(
                1L,
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5),
                AvailabilityReason.OWNER_USE
        );

        assertThat(availability.getId()).isNotNull();
        assertThat(availability.getInstrumentId()).isEqualTo(1L);
        assertThat(availability.getStartDate()).isEqualTo(LocalDate.of(2025, 12, 1));
        assertThat(availability.getEndDate()).isEqualTo(LocalDate.of(2025, 12, 5));
        assertThat(availability.getReason()).isEqualTo(AvailabilityReason.OWNER_USE);
    }

    @Test
    void shouldPersistAvailabilityToDatabase() {
        availabilityService.createUnavailability(
                2L,
                LocalDate.of(2025, 12, 10),
                LocalDate.of(2025, 12, 15),
                AvailabilityReason.MAINTENANCE
        );

        List<Availability> all = availabilityRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getInstrumentId()).isEqualTo(2L);
        assertThat(all.get(0).getReason()).isEqualTo(AvailabilityReason.MAINTENANCE);
    }

    @Test
    void shouldRejectNullStartDate() {
        assertThatThrownBy(() ->
                availabilityService.createUnavailability(1L, null, LocalDate.of(2025, 12, 5), AvailabilityReason.OWNER_USE)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Invalid dates");

        assertThat(availabilityRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectNullEndDate() {
        assertThatThrownBy(() ->
                availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 1), null, AvailabilityReason.OWNER_USE)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Invalid dates");

        assertThat(availabilityRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectStartDateAfterEndDate() {
        assertThatThrownBy(() ->
                availabilityService.createUnavailability(
                        1L,
                        LocalDate.of(2025, 12, 10),
                        LocalDate.of(2025, 12, 5),
                        AvailabilityReason.OWNER_USE
                )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Invalid dates");

        assertThat(availabilityRepository.findAll()).isEmpty();
    }

    @Test
    void shouldAllowEqualStartAndEndDates() {
        LocalDate sameDate = LocalDate.of(2025, 12, 15);
        Availability availability = availabilityService.createUnavailability(
                3L,
                sameDate,
                sameDate,
                AvailabilityReason.OTHER
        );

        assertThat(availability.getId()).isNotNull();
        assertThat(availability.getStartDate()).isEqualTo(sameDate);
        assertThat(availability.getEndDate()).isEqualTo(sameDate);
    }

    @Test
    void shouldListAllAvailabilities() {
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), AvailabilityReason.OWNER_USE);
        availabilityService.createUnavailability(2L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15), AvailabilityReason.MAINTENANCE);

        List<Availability> all = availabilityService.listAvailabilities();

        assertThat(all).hasSize(2);
    }

    @Test
    void shouldGetAvailabilitiesForSpecificInstrument() {
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), AvailabilityReason.OWNER_USE);
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15), AvailabilityReason.MAINTENANCE);
        availabilityService.createUnavailability(2L, LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25), AvailabilityReason.OTHER);

        List<Availability> instrument1Availabilities = availabilityService.getInstrumentAvailabilities(1L);

        assertThat(instrument1Availabilities).hasSize(2);
        assertThat(instrument1Availabilities).allMatch(a -> a.getInstrumentId().equals(1L));
    }

    @Test
    void shouldReturnTrueWhenNoOverlappingAvailability() {
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), AvailabilityReason.OWNER_USE);

        boolean isAvailable = availabilityService.isAvailable(1L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));

        assertThat(isAvailable).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOverlappingAvailability() {
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15), AvailabilityReason.OWNER_USE);

        boolean isAvailable = availabilityService.isAvailable(1L, LocalDate.of(2025, 12, 12), LocalDate.of(2025, 12, 17));

        assertThat(isAvailable).isFalse();
    }

    @Test
    void shouldReturnFalseWhenDateRangeFullyContained() {
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 31), AvailabilityReason.MAINTENANCE);

        boolean isAvailable = availabilityService.isAvailable(1L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));

        assertThat(isAvailable).isFalse();
    }

    @Test
    void shouldReturnFalseWhenDateRangeFullyContainsAvailability() {
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15), AvailabilityReason.OWNER_USE);

        boolean isAvailable = availabilityService.isAvailable(1L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 31));

        assertThat(isAvailable).isFalse();
    }

    @Test
    void shouldReturnFalseWhenStartDateOverlaps() {
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15), AvailabilityReason.OWNER_USE);

        boolean isAvailable = availabilityService.isAvailable(1L, LocalDate.of(2025, 12, 5), LocalDate.of(2025, 12, 12));

        assertThat(isAvailable).isFalse();
    }

    @Test
    void shouldReturnFalseWhenEndDateOverlaps() {
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15), AvailabilityReason.OWNER_USE);

        boolean isAvailable = availabilityService.isAvailable(1L, LocalDate.of(2025, 12, 13), LocalDate.of(2025, 12, 20));

        assertThat(isAvailable).isFalse();
    }

    @Test
    void shouldDeleteAvailability() {
        Availability created = availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), AvailabilityReason.OWNER_USE);
        
        assertThat(availabilityRepository.findAll()).hasSize(1);

        availabilityService.deleteUnavailability(created.getId());

        assertThat(availabilityRepository.findAll()).isEmpty();
    }

    @Test
    void shouldAllowMultipleAvailabilitiesForSameInstrument() {
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), AvailabilityReason.OWNER_USE);
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15), AvailabilityReason.MAINTENANCE);
        availabilityService.createUnavailability(1L, LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 25), AvailabilityReason.OTHER);

        List<Availability> all = availabilityService.getInstrumentAvailabilities(1L);

        assertThat(all).hasSize(3);
    }

    @Test
    void shouldHandleOwnerUseReason() {
        Availability availability = availabilityService.createUnavailability(
                5L,
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 10),
                AvailabilityReason.OWNER_USE
        );

        assertThat(availability.getReason()).isEqualTo(AvailabilityReason.OWNER_USE);
    }

    @Test
    void shouldHandleMaintenanceReason() {
        Availability availability = availabilityService.createUnavailability(
                5L,
                LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 4, 5),
                AvailabilityReason.MAINTENANCE
        );

        assertThat(availability.getReason()).isEqualTo(AvailabilityReason.MAINTENANCE);
    }

    @Test
    void shouldHandleOtherReason() {
        Availability availability = availabilityService.createUnavailability(
                5L,
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 3),
                AvailabilityReason.OTHER
        );

        assertThat(availability.getReason()).isEqualTo(AvailabilityReason.OTHER);
    }
}
