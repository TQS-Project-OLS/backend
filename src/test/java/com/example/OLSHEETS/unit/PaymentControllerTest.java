package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.PaymentController;
import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.Payment;
import com.example.OLSHEETS.data.PaymentStatus;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.repository.UserRepository;
import com.example.OLSHEETS.security.JwtUtil;
import com.example.OLSHEETS.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for PaymentController
 * Story: OLS-38 - Renter pays securely for an instrument rental
 * Epic: OLS-61
 */
@WebMvcTest(controllers = PaymentController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(PaymentControllerTest.TestConfig.class)
@DisplayName("PaymentController Unit Tests - OLS-38 Epic: OLS-61")
class PaymentControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtUtil jwtUtil() {
            return org.mockito.Mockito.mock(JwtUtil.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private UserRepository userRepository;

    private User renter;
    private Instrument instrument;
    private Booking booking;
    private Payment payment;

    @BeforeEach
    void setUp() {
        renter = new User("renter", "renter@test.com", "Renter Name", "password");
        renter.setId(1L);

        User owner = new User("owner", "owner@test.com", "Owner Name", "password");
        owner.setId(2L);

        instrument = new Instrument();
        instrument.setId(1L);
        instrument.setName("Test Guitar");
        instrument.setPrice(50.0);
        instrument.setOwner(owner);

        booking = new Booking(instrument, renter, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4));
        booking.setId(1L);
        booking.setStatus(BookingStatus.APPROVED);

        payment = new Payment(booking, BigDecimal.valueOf(150.0), "CREDIT_CARD");
        payment.setId(1L);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("TXN-12345678");
        payment.setCompletedAt(LocalDateTime.now());
    }

    private void setupSecurityContext() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("renter");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername("renter")).thenReturn(Optional.of(renter));
    }

    @Test
    @DisplayName("Should process payment successfully")
    void processPayment_ValidRequest_ReturnsSuccess() throws Exception {
        setupSecurityContext();
        when(paymentService.initiateAndProcessPayment(anyLong(), any())).thenReturn(payment);

        String requestBody = "{\"bookingId\": 1, \"paymentMethod\": \"CREDIT_CARD\", \"cardNumber\": \"4242424242424242\"}";

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.transactionId", is("TXN-12345678")));
        
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return bad request for invalid booking")
    void processPayment_InvalidBooking_ReturnsBadRequest() throws Exception {
        setupSecurityContext();
        when(paymentService.initiateAndProcessPayment(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("Booking not found"));

        String requestBody = "{\"bookingId\": 999, \"paymentMethod\": \"CREDIT_CARD\"}";

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Booking not found")));
        
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return conflict for unapproved booking")
    void processPayment_UnapprovedBooking_ReturnsConflict() throws Exception {
        setupSecurityContext();
        when(paymentService.initiateAndProcessPayment(anyLong(), any()))
                .thenThrow(new IllegalStateException("Booking is not approved"));

        String requestBody = "{\"bookingId\": 1, \"paymentMethod\": \"CREDIT_CARD\"}";

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Booking is not approved")));
        
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return conflict when payment already exists")
    void processPayment_PaymentExists_ReturnsConflict() throws Exception {
        setupSecurityContext();
        when(paymentService.initiateAndProcessPayment(anyLong(), any()))
                .thenThrow(new IllegalStateException("Payment already exists"));

        String requestBody = "{\"bookingId\": 1, \"paymentMethod\": \"CREDIT_CARD\"}";

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict());
        
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return payment by ID")
    void getPayment_ExistingPayment_ReturnsPayment() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(Optional.of(payment));

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is(1)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent payment")
    void getPayment_NonExistent_ReturnsNotFound() throws Exception {
        when(paymentService.getPaymentById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return payment by booking ID")
    void getPaymentByBooking_ExistingPayment_ReturnsPayment() throws Exception {
        when(paymentService.getPaymentByBookingId(1L)).thenReturn(Optional.of(payment));

        mockMvc.perform(get("/api/payments/booking/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", is(1)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent booking payment")
    void getPaymentByBooking_NonExistent_ReturnsNotFound() throws Exception {
        when(paymentService.getPaymentByBookingId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/booking/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should check booking payment status - paid")
    void checkPaymentStatus_PaidBooking_ReturnsTrue() throws Exception {
        when(paymentService.isBookingPaid(1L)).thenReturn(true);

        mockMvc.perform(get("/api/payments/check/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", is(1)))
                .andExpect(jsonPath("$.isPaid", is(true)));
    }

    @Test
    @DisplayName("Should check booking payment status - unpaid")
    void checkPaymentStatus_UnpaidBooking_ReturnsFalse() throws Exception {
        when(paymentService.isBookingPaid(1L)).thenReturn(false);

        mockMvc.perform(get("/api/payments/check/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", is(1)))
                .andExpect(jsonPath("$.isPaid", is(false)));
    }

    @Test
    @DisplayName("Should refund payment successfully")
    void refundPayment_CompletedPayment_ReturnsRefunded() throws Exception {
        payment.setStatus(PaymentStatus.REFUNDED);
        when(paymentService.refundPayment(1L)).thenReturn(payment);

        mockMvc.perform(post("/api/payments/1/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REFUNDED")));
    }

    @Test
    @DisplayName("Should return bad request for non-existent refund")
    void refundPayment_NonExistent_ReturnsBadRequest() throws Exception {
        when(paymentService.refundPayment(999L))
                .thenThrow(new IllegalArgumentException("Payment not found"));

        mockMvc.perform(post("/api/payments/999/refund"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Payment not found")));
    }

    @Test
    @DisplayName("Should return conflict for non-completed refund")
    void refundPayment_NotCompleted_ReturnsConflict() throws Exception {
        when(paymentService.refundPayment(1L))
                .thenThrow(new IllegalStateException("Only completed payments can be refunded"));

        mockMvc.perform(post("/api/payments/1/refund"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should cancel payment successfully")
    void cancelPayment_PendingPayment_ReturnsCancelled() throws Exception {
        payment.setStatus(PaymentStatus.CANCELLED);
        when(paymentService.cancelPayment(1L)).thenReturn(payment);

        mockMvc.perform(post("/api/payments/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Should return bad request for non-existent cancel")
    void cancelPayment_NonExistent_ReturnsBadRequest() throws Exception {
        when(paymentService.cancelPayment(999L))
                .thenThrow(new IllegalArgumentException("Payment not found"));

        mockMvc.perform(post("/api/payments/999/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Payment not found")));
    }

    @Test
    @DisplayName("Should return conflict for non-pending cancel")
    void cancelPayment_NotPending_ReturnsConflict() throws Exception {
        when(paymentService.cancelPayment(1L))
                .thenThrow(new IllegalStateException("Only pending payments can be cancelled"));

        mockMvc.perform(post("/api/payments/1/cancel"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return all payments")
    void getAllPayments_ReturnsPaymentList() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of(payment));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId", is(1)))
                .andExpect(jsonPath("$[0].status", is("COMPLETED")));
    }

    @Test
    @DisplayName("Should return empty list when no payments")
    void getAllPayments_NoPayments_ReturnsEmptyList() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    @DisplayName("Should return multiple payments")
    void getAllPayments_MultiplePayments_ReturnsAll() throws Exception {
        Payment payment2 = new Payment(booking, BigDecimal.valueOf(200.0), "DEBIT_CARD");
        payment2.setId(2L);
        payment2.setStatus(PaymentStatus.PENDING);
        when(paymentService.getAllPayments()).thenReturn(List.of(payment, payment2));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)));
    }
}
