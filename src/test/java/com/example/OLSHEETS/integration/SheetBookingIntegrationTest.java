package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetBooking;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SheetBookingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MusicSheetRepository sheetRepository;

    @Autowired
    private SheetBookingRepository bookingRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/sheets/bookings";
        bookingRepository.deleteAll();
        sheetRepository.deleteAll();
    }

    @Test
    void whenCreateValidBooking_thenSuccess() {
        MusicSheet sheet = new MusicSheet();
        sheet.setTitle("Fur Elise");
        sheet.setCategory("classical");
        sheet.setComposer("Beethoven");
        sheet.setOwnerId(1L);
        sheet.setPrice(3.50);
        sheet = sheetRepository.save(sheet);

        Map<String, Object> request = new HashMap<>();
        request.put("sheetId", sheet.getId());
        request.put("renterId", 200L);
        request.put("startDate", LocalDate.now().plusDays(1).toString());
        request.put("endDate", LocalDate.now().plusDays(3).toString());

        ResponseEntity<SheetBooking> response = restTemplate.postForEntity(baseUrl, request, SheetBooking.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRenterId()).isEqualTo(200L);
        assertThat(response.getBody().getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void whenGetBookingsByRenter_thenReturnList() {
        MusicSheet sheet = new MusicSheet();
        sheet.setTitle("Canon in D");
        sheet.setCategory("classical");
        sheet.setComposer("Pachelbel");
        sheet.setOwnerId(1L);
        sheet.setPrice(4.00);
        sheet = sheetRepository.save(sheet);

        SheetBooking booking = new SheetBooking(sheet, 300L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        bookingRepository.save(booking);

        ResponseEntity<SheetBooking[]> response = restTemplate.getForEntity(baseUrl + "/renter/300", SheetBooking[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getRenterId()).isEqualTo(300L);
    }
}

