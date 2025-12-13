package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.*;
import com.example.OLSHEETS.dto.PaymentRequest;
import com.example.OLSHEETS.repository.*;
import com.example.OLSHEETS.service.PaymentService;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for Secure Payment feature
 * Story: OLS-38 - Renter pays securely for an instrument rental
 * Epic: OLS-61
 */
public class SecurePaymentSteps {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private SheetBookingRepository sheetBookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RenterReviewRepository renterReviewRepository;

    // Scenario state
    private User authenticatedRenter;
    private User owner;
    private Instrument instrument;
    private Booking currentBooking;
    private Payment currentPayment;
    private Exception lastException;
    private Boolean isPaidResult;

    @After
    public void cleanup() {
        // Clean up database in correct order to respect FK constraints
        paymentRepository.deleteAll();
        reviewRepository.deleteAll();
        renterReviewRepository.deleteAll();
        sheetBookingRepository.deleteAll();
        bookingRepository.deleteAll();
        availabilityRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // Clear scenario state
        authenticatedRenter = null;
        owner = null;
        instrument = null;
        currentBooking = null;
        currentPayment = null;
        lastException = null;
        isPaidResult = null;
    }

    @Given("the payment system is available")
    public void thePaymentSystemIsAvailable() {
        // Payment service is autowired and available
        assertNotNull(paymentService, "Payment service should be available");
    }

    @Given("I am an authenticated renter")
    public void iAmAnAuthenticatedRenter() {
        // Create owner
        owner = new User("payment_owner", "payment_owner@test.com", "Payment Owner", "password");
        owner = userRepository.save(owner);

        // Create renter
        authenticatedRenter = new User("payment_renter", "payment_renter@test.com", "Payment Renter", "password");
        authenticatedRenter = userRepository.save(authenticatedRenter);

        // Create instrument
        instrument = new Instrument();
        instrument.setName("Payment Test Guitar");
        instrument.setDescription("Test guitar for payment scenarios");
        instrument.setOwner(owner);
        instrument.setPrice(50.0);
        instrument.setAge(2);
        instrument.setType(InstrumentType.ELECTRIC);
        instrument.setFamily(InstrumentFamily.STRING);
        instrument = itemRepository.save(instrument);
    }

    @Given("I have an approved booking for instrument with ID {int}")
    public void iHaveAnApprovedBookingForInstrumentWithID(Integer instrumentId) {
        // Use the instrument we created, not the ID from feature file
        currentBooking = new Booking(
                instrument,
                authenticatedRenter,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(4)
        );
        currentBooking.setStatus(BookingStatus.APPROVED);
        currentBooking = bookingRepository.save(currentBooking);
    }

    @Given("I have a pending booking for instrument with ID {int}")
    public void iHaveAPendingBookingForInstrumentWithID(Integer instrumentId) {
        currentBooking = new Booking(
                instrument,
                authenticatedRenter,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(4)
        );
        currentBooking.setStatus(BookingStatus.PENDING);
        currentBooking = bookingRepository.save(currentBooking);
    }

    @When("I initiate payment with method {string}")
    public void iInitiatePaymentWithMethod(String paymentMethod) {
        try {
            currentPayment = paymentService.initiatePayment(currentBooking.getId(), paymentMethod);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
            currentPayment = null;
        }
    }

    @When("I provide valid card number {string}")
    public void iProvideValidCardNumber(String cardNumber) {
        if (currentPayment != null) {
            PaymentRequest request = new PaymentRequest(currentBooking.getId(), currentPayment.getPaymentMethod());
            request.setCardNumber(cardNumber);
            try {
                currentPayment = paymentService.processPayment(currentPayment.getId(), request);
                lastException = null;
            } catch (Exception e) {
                lastException = e;
            }
        }
    }

    @When("I provide declined card number {string}")
    public void iProvideDeclinedCardNumber(String cardNumber) {
        iProvideValidCardNumber(cardNumber);
    }

    @When("I try to initiate payment with method {string}")
    public void iTryToInitiatePaymentWithMethod(String paymentMethod) {
        iInitiatePaymentWithMethod(paymentMethod);
    }

    @Then("the payment should be processed successfully")
    public void thePaymentShouldBeProcessedSuccessfully() {
        assertNull(lastException, "No exception should have occurred");
        assertNotNull(currentPayment, "Payment should exist");
        assertEquals(PaymentStatus.COMPLETED, currentPayment.getStatus(), "Payment should be completed");
    }

    @Then("the payment status should be {string}")
    public void thePaymentStatusShouldBe(String expectedStatus) {
        assertNotNull(currentPayment, "Payment should exist");
        assertEquals(expectedStatus, currentPayment.getStatus().name(), "Payment status should match");
    }

    @Then("I should receive a transaction ID")
    public void iShouldReceiveATransactionID() {
        assertNotNull(currentPayment, "Payment should exist");
        assertNotNull(currentPayment.getTransactionId(), "Transaction ID should be present");
        assertTrue(currentPayment.getTransactionId().startsWith("TXN-"), "Transaction ID should start with TXN-");
    }

    @Then("the payment should fail")
    public void thePaymentShouldFail() {
        assertNotNull(currentPayment, "Payment should exist");
        assertEquals(PaymentStatus.FAILED, currentPayment.getStatus(), "Payment should be failed");
        assertNotNull(currentPayment.getFailureReason(), "Failure reason should be present");
    }

    @Then("I should receive an error message about booking not being approved")
    public void iShouldReceiveAnErrorMessageAboutBookingNotBeingApproved() {
        assertNotNull(lastException, "Exception should have been thrown");
        assertTrue(lastException instanceof IllegalStateException, "Should be IllegalStateException");
        assertTrue(lastException.getMessage().contains("not approved"),
                "Error message should mention booking not approved");
    }

    @Given("I have a completed payment for booking ID {int}")
    public void iHaveACompletedPaymentForBookingID(Integer bookingId) {
        // Create an approved booking first
        iHaveAnApprovedBookingForInstrumentWithID(1);

        // Create and complete payment
        PaymentRequest request = new PaymentRequest(currentBooking.getId(), "CREDIT_CARD");
        request.setCardNumber("4242424242424242");
        currentPayment = paymentService.initiateAndProcessPayment(currentBooking.getId(), request);
    }

    @When("I check the payment status for booking ID {int}")
    public void iCheckThePaymentStatusForBookingID(Integer bookingId) {
        isPaidResult = paymentService.isBookingPaid(currentBooking.getId());
    }

    @Then("the system should confirm the booking is paid")
    public void theSystemShouldConfirmTheBookingIsPaid() {
        assertNotNull(isPaidResult, "Result should exist");
        assertTrue(isPaidResult, "Booking should be marked as paid");
    }

    @Given("I have a completed payment with ID {int}")
    public void iHaveACompletedPaymentWithID(Integer paymentId) {
        iHaveACompletedPaymentForBookingID(1);
    }

    @When("I request a refund for payment ID {int}")
    public void iRequestARefundForPaymentID(Integer paymentId) {
        try {
            currentPayment = paymentService.refundPayment(currentPayment.getId());
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }
}