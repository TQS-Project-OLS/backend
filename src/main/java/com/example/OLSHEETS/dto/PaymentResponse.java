package com.example.OLSHEETS.dto;

import com.example.OLSHEETS.data.Payment;
import com.example.OLSHEETS.data.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    public PaymentResponse() {
    }

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

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}