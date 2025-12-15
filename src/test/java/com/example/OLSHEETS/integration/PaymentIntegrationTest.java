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
import com.example.OLSHEETS.repository.RenterReviewRepository;
import com.example.OLSHEETS.repository.ReviewRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import com.example.OLSHEETS.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for Payment functionality
 * Story: OLS-38 - Renter pays securely for an instrument rental
 * Epic: OLS-61
 */
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

    @Autowired(required = false)
    private ReviewRepository reviewRepository;

    @Autowired(required = false)
    private RenterReviewRepository renterReviewRepository;

    private User owner;
    private User renter;
    private Instrument instrument;
    private Booking booking;

    @BeforeEach
    void cleanup() {
        paymentRepository.deleteAll();
        if (reviewRepository != null) reviewRepository.deleteAll();
        if (renterReviewRepository != null) renterReviewRepository.deleteAll();
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

    @Test
    @DisplayName("Should return conflict for rejected booking payment")
    void processPayment_RejectedBooking_ReturnsConflict() throws Exception {
        booking.setStatus(BookingStatus.REJECTED);
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

    @Test
    @DisplayName("Should return bad request for non-existent booking")
    void processPayment_NonExistentBooking_ReturnsBadRequest() throws Exception {
        String requestBody = "{\"bookingId\": 999999, \"paymentMethod\": \"CREDIT_CARD\"}";

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return conflict for duplicate payment")
    void processPayment_DuplicatePayment_ReturnsConflict() throws Exception {
        String requestBody = String.format(
            "{\"bookingId\": %d, \"paymentMethod\": \"CREDIT_CARD\", \"cardNumber\": \"4242424242424242\"}",
            booking.getId()
        );

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should check if booking is unpaid when no payment exists")
    void checkPaymentStatus_UnpaidBooking_ReturnsFalse() throws Exception {
        mockMvc.perform(get("/api/payments/check/" + booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPaid", is(false)));
    }

    @Test
    @DisplayName("Should get payment by ID")
    void getPayment_ExistingPayment_ReturnsPayment() throws Exception {
        String requestBody = String.format(
            "{\"bookingId\": %d, \"paymentMethod\": \"CREDIT_CARD\", \"cardNumber\": \"4242424242424242\"}",
            booking.getId()
        );
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElseThrow();

        mockMvc.perform(get("/api/payments/" + payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is(payment.getId().intValue())))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent payment")
    void getPayment_NonExistent_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/payments/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get payment by booking ID")
    void getPaymentByBooking_ExistingPayment_ReturnsPayment() throws Exception {
        String requestBody = String.format(
            "{\"bookingId\": %d, \"paymentMethod\": \"CREDIT_CARD\", \"cardNumber\": \"4242424242424242\"}",
            booking.getId()
        );
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/payments/booking/" + booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("Should return 404 when no payment for booking")
    void getPaymentByBooking_NonExistent_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/payments/booking/" + booking.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should refund completed payment")
    void refundPayment_CompletedPayment_Succeeds() throws Exception {
        String requestBody = String.format(
            "{\"bookingId\": %d, \"paymentMethod\": \"CREDIT_CARD\", \"cardNumber\": \"4242424242424242\"}",
            booking.getId()
        );
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElseThrow();

        mockMvc.perform(post("/api/payments/" + payment.getId() + "/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REFUNDED")));

        Payment refundedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(refundedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("Should not refund non-existent payment")
    void refundPayment_NonExistent_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/payments/999999/refund"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should cancel pending payment")
    void cancelPayment_PendingPayment_Succeeds() throws Exception {
        Payment payment = new Payment(booking, BigDecimal.valueOf(150.0), "CREDIT_CARD");
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        mockMvc.perform(post("/api/payments/" + payment.getId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        Payment cancelledPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(cancelledPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should not cancel completed payment")
    void cancelPayment_CompletedPayment_ReturnsConflict() throws Exception {
        String requestBody = String.format(
            "{\"bookingId\": %d, \"paymentMethod\": \"CREDIT_CARD\", \"cardNumber\": \"4242424242424242\"}",
            booking.getId()
        );
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElseThrow();

        mockMvc.perform(post("/api/payments/" + payment.getId() + "/cancel"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should find payment by booking ID in repository")
    void findByBookingId_ExistingPayment_ReturnsPayment() {
        Payment payment = new Payment(booking, BigDecimal.valueOf(150.0), "CREDIT_CARD");
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        assertThat(paymentRepository.findByBookingId(booking.getId())).isPresent();
    }

    @Test
    @DisplayName("Should check if payment exists by booking ID")
    void existsByBookingId_ExistingPayment_ReturnsTrue() {
        Payment payment = new Payment(booking, BigDecimal.valueOf(150.0), "CREDIT_CARD");
        paymentRepository.save(payment);

        assertThat(paymentRepository.existsByBookingId(booking.getId())).isTrue();
        assertThat(paymentRepository.existsByBookingId(999999L)).isFalse();
    }

    @Test
    @DisplayName("Should find payments by status")
    void findByStatus_MultiplePayments_ReturnsMatchingStatus() {
        Payment payment1 = new Payment(booking, BigDecimal.valueOf(150.0), "CREDIT_CARD");
        payment1.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment1);

        Booking booking2 = new Booking(instrument, renter, LocalDate.now().plusDays(10), LocalDate.now().plusDays(14));
        booking2.setStatus(BookingStatus.APPROVED);
        booking2 = bookingRepository.save(booking2);

        Payment payment2 = new Payment(booking2, BigDecimal.valueOf(200.0), "CREDIT_CARD");
        payment2.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment2);

        List<Payment> completedPayments = paymentRepository.findByStatus(PaymentStatus.COMPLETED);
        List<Payment> pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);

        assertThat(completedPayments).hasSize(1);
        assertThat(pendingPayments).hasSize(1);
    }

    @Test
    @DisplayName("Should find payments by renter ID")
    void findByBookingRenterId_ExistingPayments_ReturnsPayments() {
        Payment payment = new Payment(booking, BigDecimal.valueOf(150.0), "CREDIT_CARD");
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        List<Payment> renterPayments = paymentRepository.findByBookingRenterId(renter.getId());

        assertThat(renterPayments).hasSize(1);
        assertThat(renterPayments.get(0).getBooking().getRenter().getId()).isEqualTo(renter.getId());
    }
}
