package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.SheetBookingController;
import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetBooking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.service.SheetBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SheetBookingController.class)
class SheetBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SheetBookingService bookingService;

    private MusicSheet sheet;
    private SheetBooking booking;

    @BeforeEach
    void setUp() {
        sheet = new MusicSheet();
        sheet.setTitle("Moonlight Sonata");
        sheet.setCategory("classical");
        sheet.setComposer("Beautiful piece");
        sheet.setPrice(5.00);
        sheet.setOwnerId(1L);
        sheet.setId(1L);

        booking = new SheetBooking(sheet, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        booking.setId(1L);
    }

    @Test
    void whenCreateBooking_thenReturn201() throws Exception {
        when(bookingService.createBooking(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(booking);

        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate = LocalDate.now().plusDays(3).toString();

        mockMvc.perform(post("/api/sheets/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sheetId\":1,\"renterId\":100,\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate + "\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.renterId").value(100));
    }

    @Test
    void whenGetBookingsByRenter_thenReturn200() throws Exception {
        when(bookingService.getBookingsByRenter(100L)).thenReturn(List.of(booking));

        mockMvc.perform(get("/api/sheets/bookings/renter/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].renterId").value(100));
    }

    @Test
    void whenGetBookingById_thenReturn200() throws Exception {
        when(bookingService.getBookingById(1L)).thenReturn(Optional.of(booking));

        mockMvc.perform(get("/api/sheets/bookings/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void whenGetNonExistentBooking_thenReturn404() throws Exception {
        when(bookingService.getBookingById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sheets/bookings/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void whenCreateBookingWithInvalidData_thenReturn400() throws Exception {
        when(bookingService.createBooking(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenThrow(new IllegalArgumentException("Start date must be before end date"));

        String startDate = LocalDate.now().plusDays(5).toString();
        String endDate = LocalDate.now().plusDays(2).toString();

        mockMvc.perform(post("/api/sheets/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sheetId\":1,\"renterId\":100,\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate + "\"}"))
            .andExpect(status().isBadRequest());
    }
}

