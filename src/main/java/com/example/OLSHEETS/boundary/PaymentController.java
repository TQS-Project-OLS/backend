package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.Payment;
import com.example.OLSHEETS.dto.PaymentRequest;
import com.example.OLSHEETS.dto.PaymentResponse;
import com.example.OLSHEETS.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest request) {
        try {
            Payment payment = paymentService.initiateAndProcessPayment(request.getBookingId(), request);
            return ResponseEntity.ok(new PaymentResponse(payment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable Long paymentId) {
        Optional<Payment> payment = paymentService.getPaymentById(paymentId);
        if (payment.isPresent()) {
            return ResponseEntity.ok(new PaymentResponse(payment.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/check/{bookingId}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable Long bookingId) {
        boolean isPaid = paymentService.isBookingPaid(bookingId);
        return ResponseEntity.ok(Map.of("bookingId", bookingId, "isPaid", isPaid));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getPaymentByBooking(@PathVariable Long bookingId) {
        Optional<Payment> payment = paymentService.getPaymentByBookingId(bookingId);
        if (payment.isPresent()) {
            return ResponseEntity.ok(new PaymentResponse(payment.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<?> refundPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.refundPayment(paymentId);
            return ResponseEntity.ok(new PaymentResponse(payment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.cancelPayment(paymentId);
            return ResponseEntity.ok(new PaymentResponse(payment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::new)
                .toList();
        return ResponseEntity.ok(responses);
    }
}