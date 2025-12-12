package com.example.OLSHEETS.dto;

import com.example.OLSHEETS.data.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long paymentId;
    private Long bookingId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String failureReason;

    public PaymentResponse(Payment payment) {
        this.paymentId = payment.getId();
        this.bookingId = payment.getBooking() != null ? payment.getBooking().getId() : null;
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.status = payment.getStatus() != null ? payment.getStatus().name() : null;
        this.transactionId = payment.getTransactionId();
        this.createdAt = payment.getCreatedAt();
        this.completedAt = payment.getCompletedAt();
        this.failureReason = payment.getFailureReason();
    }
}