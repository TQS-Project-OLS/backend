package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.Payment;
import com.example.OLSHEETS.data.PaymentStatus;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.dto.PaymentRequest;
import com.example.OLSHEETS.dto.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Payment entity and DTOs
 * Story: OLS-38 - Renter pays securely for an instrument rental
 * Epic: OLS-61
 */
@DisplayName("Payment Entity and DTO Tests - OLS-38 Epic: OLS-61")
class PaymentEntityDtoTest {

    private User owner;
    private User renter;
    private Instrument instrument;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "owner@test.com", "Owner Name", "password");
        owner.setId(1L);

        renter = new User("renter", "renter@test.com", "Renter Name", "password");
        renter.setId(2L);

        instrument = new Instrument();
        instrument.setId(1L);
        instrument.setName("Test Guitar");
        instrument.setPrice(50.0);
        instrument.setOwner(owner);

        booking = new Booking(instrument, renter, LocalDate.now().plusDays(1), LocalDate.now().plusDays(4));
        booking.setId(1L);
        booking.setStatus(BookingStatus.APPROVED);
    }

    @Nested
    @DisplayName("Payment Entity Tests - OLS-38")
    class PaymentEntityTests {

        @Test
        @DisplayName("Should create payment with default constructor")
        void payment_DefaultConstructor_SetsDefaults() {
            Payment payment = new Payment();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should create payment with parameterized constructor")
        void payment_ParameterizedConstructor_SetsValues() {
            Payment payment = new Payment(booking, BigDecimal.valueOf(150.0), "CREDIT_CARD");
            assertThat(payment.getBooking()).isEqualTo(booking);
            assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.0));
            assertThat(payment.getPaymentMethod()).isEqualTo("CREDIT_CARD");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("Should set and get all Payment properties")
        void payment_SettersAndGetters_WorkCorrectly() {
            Payment payment = new Payment();
            LocalDateTime now = LocalDateTime.now();
            
            payment.setId(1L);
            payment.setBooking(booking);
            payment.setAmount(BigDecimal.valueOf(100.0));
            payment.setPaymentMethod("DEBIT_CARD");
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId("TXN-12345678");
            payment.setCreatedAt(now);
            payment.setCompletedAt(now.plusMinutes(5));
            payment.setFailureReason("Test failure");

            assertThat(payment.getId()).isEqualTo(1L);
            assertThat(payment.getBooking()).isEqualTo(booking);
            assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
            assertThat(payment.getPaymentMethod()).isEqualTo("DEBIT_CARD");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(payment.getTransactionId()).isEqualTo("TXN-12345678");
            assertThat(payment.getCreatedAt()).isEqualTo(now);
            assertThat(payment.getCompletedAt()).isEqualTo(now.plusMinutes(5));
            assertThat(payment.getFailureReason()).isEqualTo("Test failure");
        }

        @Test
        @DisplayName("Should handle all PaymentStatus values")
        void payment_AllStatusValues_AreHandled() {
            Payment payment = new Payment();
            
            for (PaymentStatus status : PaymentStatus.values()) {
                payment.setStatus(status);
                assertThat(payment.getStatus()).isEqualTo(status);
            }
        }
    }

    @Nested
    @DisplayName("PaymentStatus Enum Tests - OLS-38")
    class PaymentStatusEnumTests {

        @Test
        @DisplayName("Should have all required status values")
        void paymentStatus_AllValuesExist() {
            assertThat(PaymentStatus.values()).containsExactly(
                PaymentStatus.PENDING,
                PaymentStatus.COMPLETED,
                PaymentStatus.FAILED,
                PaymentStatus.REFUNDED,
                PaymentStatus.CANCELLED
            );
        }

        @Test
        @DisplayName("Should convert status to string correctly")
        void paymentStatus_ToStringConversion_WorksCorrectly() {
            assertThat(PaymentStatus.PENDING.name()).isEqualTo("PENDING");
            assertThat(PaymentStatus.COMPLETED.name()).isEqualTo("COMPLETED");
            assertThat(PaymentStatus.FAILED.name()).isEqualTo("FAILED");
            assertThat(PaymentStatus.REFUNDED.name()).isEqualTo("REFUNDED");
            assertThat(PaymentStatus.CANCELLED.name()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("Should parse status from string correctly")
        void paymentStatus_FromStringConversion_WorksCorrectly() {
            assertThat(PaymentStatus.valueOf("PENDING")).isEqualTo(PaymentStatus.PENDING);
            assertThat(PaymentStatus.valueOf("COMPLETED")).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(PaymentStatus.valueOf("FAILED")).isEqualTo(PaymentStatus.FAILED);
            assertThat(PaymentStatus.valueOf("REFUNDED")).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(PaymentStatus.valueOf("CANCELLED")).isEqualTo(PaymentStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("PaymentRequest DTO Tests - OLS-38")
    class PaymentRequestDtoTests {

        @Test
        @DisplayName("Should create PaymentRequest with default constructor")
        void paymentRequest_DefaultConstructor_CreatesEmpty() {
            PaymentRequest request = new PaymentRequest();
            assertThat(request.getBookingId()).isNull();
            assertThat(request.getPaymentMethod()).isNull();
            assertThat(request.getCardNumber()).isNull();
        }

        @Test
        @DisplayName("Should create PaymentRequest with parameterized constructor")
        void paymentRequest_ParameterizedConstructor_SetsValues() {
            PaymentRequest request = new PaymentRequest(1L, "CREDIT_CARD");
            assertThat(request.getBookingId()).isEqualTo(1L);
            assertThat(request.getPaymentMethod()).isEqualTo("CREDIT_CARD");
        }

        @Test
        @DisplayName("Should set and get all PaymentRequest properties")
        void paymentRequest_SettersAndGetters_WorkCorrectly() {
            PaymentRequest request = new PaymentRequest();
            request.setBookingId(2L);
            request.setPaymentMethod("DEBIT_CARD");
            request.setCardNumber("4242424242424242");

            assertThat(request.getBookingId()).isEqualTo(2L);
            assertThat(request.getPaymentMethod()).isEqualTo("DEBIT_CARD");
            assertThat(request.getCardNumber()).isEqualTo("4242424242424242");
        }
    }

    @Nested
    @DisplayName("PaymentResponse DTO Tests - OLS-38")
    class PaymentResponseDtoTests {

        @Test
        @DisplayName("Should create PaymentResponse with default constructor")
        void paymentResponse_DefaultConstructor_CreatesEmpty() {
            PaymentResponse response = new PaymentResponse();
            assertThat(response.getPaymentId()).isNull();
            assertThat(response.getBookingId()).isNull();
            assertThat(response.getAmount()).isNull();
        }

        @Test
        @DisplayName("Should create PaymentResponse from Payment entity")
        void paymentResponse_FromPaymentConstructor_CopiesValues() {
            Payment payment = new Payment(booking, BigDecimal.valueOf(150.0), "CREDIT_CARD");
            payment.setId(1L);
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId("TXN-12345678");
            payment.setCompletedAt(LocalDateTime.now());
            payment.setFailureReason(null);

            PaymentResponse response = new PaymentResponse(payment);

            assertThat(response.getPaymentId()).isEqualTo(1L);
            assertThat(response.getBookingId()).isEqualTo(1L);
            assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.0));
            assertThat(response.getPaymentMethod()).isEqualTo("CREDIT_CARD");
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getTransactionId()).isEqualTo("TXN-12345678");
            assertThat(response.getCompletedAt()).isNotNull();
            assertThat(response.getFailureReason()).isNull();
        }

        @Test
        @DisplayName("Should handle Payment with null booking")
        void paymentResponse_NullBooking_SetsNullBookingId() {
            Payment payment = new Payment();
            payment.setId(1L);
            payment.setAmount(BigDecimal.valueOf(100.0));
            payment.setPaymentMethod("CREDIT_CARD");
            payment.setStatus(PaymentStatus.PENDING);

            PaymentResponse response = new PaymentResponse(payment);

            assertThat(response.getPaymentId()).isEqualTo(1L);
            assertThat(response.getBookingId()).isNull();
        }

        @Test
        @DisplayName("Should handle Payment with null status")
        void paymentResponse_NullStatus_SetsNullStatus() {
            Payment payment = new Payment();
            payment.setId(1L);
            payment.setBooking(booking);
            payment.setAmount(BigDecimal.valueOf(100.0));
            payment.setPaymentMethod("CREDIT_CARD");
            // Explicitly set status to null (overriding constructor default)
            java.lang.reflect.Field statusField;
            try {
                statusField = Payment.class.getDeclaredField("status");
                statusField.setAccessible(true);
                statusField.set(payment, null);
            } catch (Exception e) {
                // Skip if reflection fails
                return;
            }

            PaymentResponse response = new PaymentResponse(payment);

            assertThat(response.getStatus()).isNull();
        }

        @Test
        @DisplayName("Should set and get all PaymentResponse properties")
        void paymentResponse_SettersAndGetters_WorkCorrectly() {
            PaymentResponse response = new PaymentResponse();
            LocalDateTime now = LocalDateTime.now();

            response.setPaymentId(1L);
            response.setBookingId(2L);
            response.setAmount(BigDecimal.valueOf(150.0));
            response.setPaymentMethod("CREDIT_CARD");
            response.setStatus("COMPLETED");
            response.setTransactionId("TXN-12345678");
            response.setCreatedAt(now);
            response.setCompletedAt(now.plusMinutes(5));
            response.setFailureReason("Test failure");

            assertThat(response.getPaymentId()).isEqualTo(1L);
            assertThat(response.getBookingId()).isEqualTo(2L);
            assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.0));
            assertThat(response.getPaymentMethod()).isEqualTo("CREDIT_CARD");
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getTransactionId()).isEqualTo("TXN-12345678");
            assertThat(response.getCreatedAt()).isEqualTo(now);
            assertThat(response.getCompletedAt()).isEqualTo(now.plusMinutes(5));
            assertThat(response.getFailureReason()).isEqualTo("Test failure");
        }

        @Test
        @DisplayName("Should handle failed payment response")
        void paymentResponse_FailedPayment_SetsFailureReason() {
            Payment payment = new Payment(booking, BigDecimal.valueOf(150.0), "CREDIT_CARD");
            payment.setId(1L);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Card declined");

            PaymentResponse response = new PaymentResponse(payment);

            assertThat(response.getStatus()).isEqualTo("FAILED");
            assertThat(response.getFailureReason()).isEqualTo("Card declined");
            assertThat(response.getTransactionId()).isNull();
        }
    }
}
