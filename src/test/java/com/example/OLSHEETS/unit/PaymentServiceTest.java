package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.Payment;
import com.example.OLSHEETS.data.PaymentStatus;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.dto.PaymentRequest;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.repository.PaymentRepository;
import com.example.OLSHEETS.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests - OLS-38 Epic: OLS-61")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentService paymentService;

    private User renter;
    private User owner;
    private Instrument instrument;
    private Booking booking;
    private Payment payment;

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

        payment = new Payment(booking, BigDecimal.valueOf(150.0), "CREDIT_CARD");
        payment.setId(1L);
    }

    @Nested
    @DisplayName("Calculate Booking Amount Tests - OLS-38")
    class CalculateBookingAmountTests {

        @Test
        @DisplayName("Should calculate correct amount for multi-day rental")
        void calculateBookingAmount_MultiDayRental_CalculatesCorrectly() {
            BigDecimal amount = paymentService.calculateBookingAmount(booking);
            assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(150.0));
        }

        @Test
        @DisplayName("Should calculate minimum 1 day for same-day booking")
        void calculateBookingAmount_SameDayRental_MinimumOneDay() {
            Booking sameDayBooking = new Booking(instrument, renter, LocalDate.now(), LocalDate.now());
            sameDayBooking.setStatus(BookingStatus.APPROVED);
            BigDecimal amount = paymentService.calculateBookingAmount(sameDayBooking);
            assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        }

        @Test
        @DisplayName("Should throw exception for null booking")
        void calculateBookingAmount_NullBooking_ThrowsException() {
            assertThatThrownBy(() -> paymentService.calculateBookingAmount(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Should calculate correct amount for single day rental")
        void calculateBookingAmount_SingleDayRental_CalculatesCorrectly() {
            Booking singleDayBooking = new Booking(instrument, renter, LocalDate.now(), LocalDate.now().plusDays(1));
            singleDayBooking.setStatus(BookingStatus.APPROVED);
            BigDecimal amount = paymentService.calculateBookingAmount(singleDayBooking);
            assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        }

        @Test
        @DisplayName("Should calculate correct amount for long-term rental")
        void calculateBookingAmount_LongTermRental_CalculatesCorrectly() {
            Booking longTermBooking = new Booking(instrument, renter, LocalDate.now(), LocalDate.now().plusDays(30));
            longTermBooking.setStatus(BookingStatus.APPROVED);
            BigDecimal amount = paymentService.calculateBookingAmount(longTermBooking);
            assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(1500.0));
        }
    }

    @Nested
    @DisplayName("Initiate Payment Tests - OLS-38")
    class InitiatePaymentTests {

        @Test
        @DisplayName("Should initiate payment for approved booking")
        void initiatePayment_ApprovedBooking_CreatesPayment() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(paymentRepository.existsByBookingId(1L)).thenReturn(false);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            Payment result = paymentService.initiatePayment(1L, "CREDIT_CARD");

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(result.getPaymentMethod()).isEqualTo("CREDIT_CARD");
            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.0));
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should throw exception for non-existent booking")
        void initiatePayment_NonExistentBooking_ThrowsException() {
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> paymentService.initiatePayment(999L, "CREDIT_CARD"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Booking not found");
        }

        @Test
        @DisplayName("Should throw exception for pending booking")
        void initiatePayment_PendingBooking_ThrowsException() {
            booking.setStatus(BookingStatus.PENDING);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            assertThatThrownBy(() -> paymentService.initiatePayment(1L, "CREDIT_CARD"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not approved");
        }

        @Test
        @DisplayName("Should throw exception when payment already exists")
        void initiatePayment_PaymentExists_ThrowsException() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(paymentRepository.existsByBookingId(1L)).thenReturn(true);
            assertThatThrownBy(() -> paymentService.initiatePayment(1L, "CREDIT_CARD"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw exception for rejected booking")
        void initiatePayment_RejectedBooking_ThrowsException() {
            booking.setStatus(BookingStatus.REJECTED);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            assertThatThrownBy(() -> paymentService.initiatePayment(1L, "CREDIT_CARD"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not approved");
        }

        @Test
        @DisplayName("Should throw exception for cancelled booking")
        void initiatePayment_CancelledBooking_ThrowsException() {
            booking.setStatus(BookingStatus.CANCELLED);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            assertThatThrownBy(() -> paymentService.initiatePayment(1L, "CREDIT_CARD"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not approved");
        }

        @Test
        @DisplayName("Should initiate payment with different payment method")
        void initiatePayment_DebitCard_CreatesPayment() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(paymentRepository.existsByBookingId(1L)).thenReturn(false);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            Payment result = paymentService.initiatePayment(1L, "DEBIT_CARD");

            assertThat(result.getPaymentMethod()).isEqualTo("DEBIT_CARD");
        }
    }

    @Nested
    @DisplayName("Process Payment Tests - OLS-38")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Should process payment successfully with valid card")
        void processPayment_ValidCard_Succeeds() {
            PaymentRequest request = new PaymentRequest(1L, "CREDIT_CARD");
            request.setCardNumber("4242424242424242");
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.processPayment(1L, request);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(result.getTransactionId()).isNotNull();
            assertThat(result.getTransactionId()).startsWith("TXN-");
            assertThat(result.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should fail payment with declined card")
        void processPayment_DeclinedCard_Fails() {
            PaymentRequest request = new PaymentRequest(1L, "CREDIT_CARD");
            request.setCardNumber("4000000000000002");
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.processPayment(1L, request);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(result.getFailureReason()).isNotNull();
            assertThat(result.getFailureReason()).isEqualTo("Card declined");
        }

        @Test
        @DisplayName("Should throw exception for non-existent payment")
        void processPayment_NonExistentPayment_ThrowsException() {
            PaymentRequest request = new PaymentRequest(999L, "CREDIT_CARD");
            when(paymentRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> paymentService.processPayment(999L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payment not found");
        }

        @Test
        @DisplayName("Should process payment with null card number as success")
        void processPayment_NullCardNumber_Succeeds() {
            PaymentRequest request = new PaymentRequest(1L, "CREDIT_CARD");
            request.setCardNumber(null);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.processPayment(1L, request);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should process payment with any valid card number")
        void processPayment_AnyValidCard_Succeeds() {
            PaymentRequest request = new PaymentRequest(1L, "CREDIT_CARD");
            request.setCardNumber("5555555555554444");
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.processPayment(1L, request);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("Initiate And Process Payment Tests - OLS-38")
    class InitiateAndProcessPaymentTests {

        @Test
        @DisplayName("Should initiate and process payment in one step")
        void initiateAndProcessPayment_ValidRequest_Succeeds() {
            PaymentRequest request = new PaymentRequest(1L, "CREDIT_CARD");
            request.setCardNumber("4242424242424242");
            
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(paymentRepository.existsByBookingId(1L)).thenReturn(false);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            Payment result = paymentService.initiateAndProcessPayment(1L, request);

            assertThat(result).isNotNull();
            verify(bookingRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("Refund Payment Tests - OLS-38")
    class RefundPaymentTests {

        @Test
        @DisplayName("Should refund completed payment")
        void refundPayment_CompletedPayment_Succeeds() {
            payment.setStatus(PaymentStatus.COMPLETED);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.refundPayment(1L);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("Should throw exception for non-existent payment refund")
        void refundPayment_NonExistentPayment_ThrowsException() {
            when(paymentRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> paymentService.refundPayment(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payment not found");
        }

        @Test
        @DisplayName("Should throw exception for pending payment refund")
        void refundPayment_PendingPayment_ThrowsException() {
            payment.setStatus(PaymentStatus.PENDING);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            assertThatThrownBy(() -> paymentService.refundPayment(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only completed payments can be refunded");
        }

        @Test
        @DisplayName("Should throw exception for failed payment refund")
        void refundPayment_FailedPayment_ThrowsException() {
            payment.setStatus(PaymentStatus.FAILED);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            assertThatThrownBy(() -> paymentService.refundPayment(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only completed payments can be refunded");
        }

        @Test
        @DisplayName("Should throw exception for already refunded payment")
        void refundPayment_AlreadyRefundedPayment_ThrowsException() {
            payment.setStatus(PaymentStatus.REFUNDED);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            assertThatThrownBy(() -> paymentService.refundPayment(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only completed payments can be refunded");
        }
    }

    @Nested
    @DisplayName("Cancel Payment Tests - OLS-38")
    class CancelPaymentTests {

        @Test
        @DisplayName("Should cancel pending payment")
        void cancelPayment_PendingPayment_Succeeds() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.cancelPayment(1L);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should throw exception for non-existent payment cancel")
        void cancelPayment_NonExistentPayment_ThrowsException() {
            when(paymentRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> paymentService.cancelPayment(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payment not found");
        }

        @Test
        @DisplayName("Should throw exception for completed payment cancel")
        void cancelPayment_CompletedPayment_ThrowsException() {
            payment.setStatus(PaymentStatus.COMPLETED);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            assertThatThrownBy(() -> paymentService.cancelPayment(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only pending payments can be cancelled");
        }

        @Test
        @DisplayName("Should throw exception for failed payment cancel")
        void cancelPayment_FailedPayment_ThrowsException() {
            payment.setStatus(PaymentStatus.FAILED);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            assertThatThrownBy(() -> paymentService.cancelPayment(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only pending payments can be cancelled");
        }
    }

    @Nested
    @DisplayName("Check Booking Paid Tests - OLS-38")
    class CheckBookingPaidTests {

        @Test
        @DisplayName("Should return true for paid booking")
        void isBookingPaid_CompletedPayment_ReturnsTrue() {
            payment.setStatus(PaymentStatus.COMPLETED);
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(payment));
            boolean result = paymentService.isBookingPaid(1L);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for no payment")
        void isBookingPaid_NoPayment_ReturnsFalse() {
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.empty());
            boolean result = paymentService.isBookingPaid(1L);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for pending payment")
        void isBookingPaid_PendingPayment_ReturnsFalse() {
            payment.setStatus(PaymentStatus.PENDING);
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(payment));
            boolean result = paymentService.isBookingPaid(1L);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for failed payment")
        void isBookingPaid_FailedPayment_ReturnsFalse() {
            payment.setStatus(PaymentStatus.FAILED);
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(payment));
            boolean result = paymentService.isBookingPaid(1L);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for refunded payment")
        void isBookingPaid_RefundedPayment_ReturnsFalse() {
            payment.setStatus(PaymentStatus.REFUNDED);
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(payment));
            boolean result = paymentService.isBookingPaid(1L);
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Get Payment Methods Tests - OLS-38")
    class GetPaymentMethodsTests {

        @Test
        @DisplayName("Should get payment by ID when exists")
        void getPaymentById_Exists_ReturnsPayment() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            Optional<Payment> result = paymentService.getPaymentById(1L);
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return empty when payment not found")
        void getPaymentById_NotExists_ReturnsEmpty() {
            when(paymentRepository.findById(999L)).thenReturn(Optional.empty());
            Optional<Payment> result = paymentService.getPaymentById(999L);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should get payment by booking ID when exists")
        void getPaymentByBookingId_Exists_ReturnsPayment() {
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(payment));
            Optional<Payment> result = paymentService.getPaymentByBookingId(1L);
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("Should return empty when payment not found by booking ID")
        void getPaymentByBookingId_NotExists_ReturnsEmpty() {
            when(paymentRepository.findByBookingId(999L)).thenReturn(Optional.empty());
            Optional<Payment> result = paymentService.getPaymentByBookingId(999L);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should get all payments")
        void getAllPayments_ReturnsAllPayments() {
            Payment payment2 = new Payment(booking, BigDecimal.valueOf(200.0), "DEBIT_CARD");
            payment2.setId(2L);
            when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment, payment2));
            List<Payment> result = paymentService.getAllPayments();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no payments")
        void getAllPayments_NoPayments_ReturnsEmptyList() {
            when(paymentRepository.findAll()).thenReturn(Collections.emptyList());
            List<Payment> result = paymentService.getAllPayments();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should get payments by renter ID")
        void getPaymentsByRenterId_Exists_ReturnsPayments() {
            when(paymentRepository.findByBookingRenterId(2L)).thenReturn(Arrays.asList(payment));
            List<Payment> result = paymentService.getPaymentsByRenterId(2L);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no payments for renter")
        void getPaymentsByRenterId_NoPayments_ReturnsEmptyList() {
            when(paymentRepository.findByBookingRenterId(999L)).thenReturn(Collections.emptyList());
            List<Payment> result = paymentService.getPaymentsByRenterId(999L);
            assertThat(result).isEmpty();
        }
    }
}
