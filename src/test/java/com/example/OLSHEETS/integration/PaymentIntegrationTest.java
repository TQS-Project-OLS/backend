package com.example.OLSHEETS.integration;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.Payment;
import com.example.OLSHEETS.data.PaymentStatus;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.repository.AvailabilityRepository;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.repository.ItemRepository;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import com.example.OLSHEETS.repository.PaymentRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import com.example.OLSHEETS.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
    "spring.main.lazy-initialization=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Payment Integration Tests - OLS-38 Epic: OLS-61")
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SheetBookingRepository sheetBookingRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    private User owner;
    private User renter;
    private Instrument instrument;
    private Booking booking;

    @BeforeEach
    void cleanup() {
        paymentRepository.deleteAll();
        sheetBookingRepository.deleteAll();
        bookingRepository.deleteAll();
        availabilityRepository.deleteAll();
        musicSheetRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User("owner", "owner@test.com", "Owner Name", "password");
        owner = userRepository.save(owner);

        renter = new User("renter", "renter@test.com", "Renter Name", "password");
        renter = userRepository.save(renter);

        instrument = new Instrument();
        instrument.setName("Test Guitar");
        instrument.setDescription("A test guitar");
        instrument.setOwner(owner);
        instrument.setPrice(50.0);
        instrument.setAge(2);
        instrument.setType(InstrumentType.ELECTRIC);
        instrument.setFamily(InstrumentFamily.STRING);
        instrument = itemRepository.save(instrument);

        booking = new Booking(instrument, renter, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4));
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);
    }

    @Test
    @DisplayName("Should process payment for approved booking")
    void processPayment_ApprovedBooking_CreatesAndCompletesPayment() throws Exception {
        String requestBody = String.format(
            "{\"bookingId\": %d, \"paymentMethod\": \"CREDIT_CARD\", \"cardNumber\": \"4242424242424242\"}",
            booking.getId()
        );

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.bookingId", is(booking.getId().intValue())));

        Payment savedPayment = paymentRepository.findByBookingId(booking.getId()).orElseThrow();
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(savedPayment.getTransactionId()).isNotNull();
    }

    @Test
    @DisplayName("Should fail payment with declined card")
    void processPayment_DeclinedCard_FailsPayment() throws Exception {
        String requestBody = String.format(
            "{\"bookingId\": %d, \"paymentMethod\": \"CREDIT_CARD\", \"cardNumber\": \"4000000000000002\"}",
            booking.getId()
        );

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("FAILED")));

        Payment savedPayment = paymentRepository.findByBookingId(booking.getId()).orElseThrow();
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Should check if booking is paid")
    void checkPaymentStatus_PaidBooking_ReturnsTrue() throws Exception {
        String requestBody = String.format(
            "{\"bookingId\": %d, \"paymentMethod\": \"CREDIT_CARD\", \"cardNumber\": \"4242424242424242\"}",
            booking.getId()
        );
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/payments/check/" + booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPaid", is(true)));
    }

    @Test
    @DisplayName("Should return conflict for pending booking payment")
    void processPayment_PendingBooking_ReturnsConflict() throws Exception {
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        String requestBody = String.format(
            "{\"bookingId\": %d, \"paymentMethod\": \"CREDIT_CARD\"}",
            booking.getId()
        );

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict());
    }
}
