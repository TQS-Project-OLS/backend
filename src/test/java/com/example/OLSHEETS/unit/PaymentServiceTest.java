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
    @DisplayName("Calculate Booking Amount Tests")
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
    }

    @Nested
    @DisplayName("Initiate Payment Tests")
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
    }

    @Nested
    @DisplayName("Process Payment Tests")
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
        }
    }

    @Nested
    @DisplayName("Refund and Cancel Payment Tests")
    class RefundCancelTests {

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
        @DisplayName("Should cancel pending payment")
        void cancelPayment_PendingPayment_Succeeds() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
            Payment result = paymentService.cancelPayment(1L);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("Check Booking Paid Tests")
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
    }
}
