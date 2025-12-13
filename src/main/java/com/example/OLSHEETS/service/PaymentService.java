package com.example.OLSHEETS.service;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Payment;
import com.example.OLSHEETS.data.PaymentStatus;
import com.example.OLSHEETS.dto.PaymentRequest;
import com.example.OLSHEETS.repository.BookingRepository;
import com.example.OLSHEETS.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private static final String DECLINED_CARD = "4000000000000002";

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    public BigDecimal calculateBookingAmount(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        long days = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
        if (days < 1) {
            days = 1;
        }
        Double pricePerDay = booking.getItem().getPrice();
        return BigDecimal.valueOf(pricePerDay * days);
    }

    @Transactional
    public Payment initiatePayment(Long bookingId, String paymentMethod) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new IllegalStateException("Booking is not approved for payment");
        }

        if (paymentRepository.existsByBookingId(bookingId)) {
            throw new IllegalStateException("Payment already exists for this booking");
        }

        BigDecimal amount = calculateBookingAmount(booking);
        Payment payment = new Payment(booking, amount, paymentMethod);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment processPayment(Long paymentId, PaymentRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + paymentId));

        String cardNumber = request.getCardNumber();
        if (cardNumber != null && cardNumber.equals(DECLINED_CARD)) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Card declined");
            return paymentRepository.save(payment);
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setCompletedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment initiateAndProcessPayment(Long bookingId, PaymentRequest request) {
        Payment payment = initiatePayment(bookingId, request.getPaymentMethod());
        return processPayment(payment.getId(), request);
    }

    @Transactional
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        return paymentRepository.save(payment);
    }

    public boolean isBookingPaid(Long bookingId) {
        Optional<Payment> payment = paymentRepository.findByBookingId(bookingId);
        return payment.isPresent() && payment.get().getStatus() == PaymentStatus.COMPLETED;
    }

    public Optional<Payment> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public Optional<Payment> getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByRenterId(Long renterId) {
        return paymentRepository.findByBookingRenterId(renterId);
    }
}