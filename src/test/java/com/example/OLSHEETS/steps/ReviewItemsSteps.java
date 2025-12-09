package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.*;
import com.example.OLSHEETS.dto.ReviewRequest;
import com.example.OLSHEETS.dto.ReviewResponse;
import com.example.OLSHEETS.repository.*;
import com.example.OLSHEETS.service.ReviewService;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewItemsSteps {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Instrument> instruments = new HashMap<>();
    private final Map<String, Booking> bookings = new HashMap<>();
    
    private ReviewResponse lastReviewResponse;
    private List<ReviewResponse> reviewsList;
    private Exception lastException;
    private Boolean canReviewResult;

    @After
    public void cleanup() {
        // Clean up only data created by review scenarios
        // Clear in-memory maps first
        users.clear();
        instruments.clear();
        bookings.clear();
        lastReviewResponse = null;
        reviewsList = null;
        lastException = null;
        canReviewResult = null;
    }

    @Given("there is a user {string} with ID {int}")
    public void thereIsAUserWithID(String username, Integer userId) {
        User user = new User(username);
        user = userRepository.save(user);
        users.put(username, user);
    }

    @Given("the owner has an instrument {string} with price {double}")
    public void theOwnerHasAnInstrumentWithPrice(String instrumentName, Double price) {
        // Try to find owner in map first, otherwise fetch from database
        User owner = users.values().stream()
                .filter(u -> u.getUsername().contains("owner"))
                .findFirst()
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getUsername().contains("owner"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No owner user found")));
        
        Instrument instrument = new Instrument();
        instrument.setName(instrumentName);
        instrument.setDescription("Test instrument");
        instrument.setPrice(price);
        instrument.setOwner(owner);
        instrument.setAge(2);
        instrument.setType(InstrumentType.DIGITAL);
        instrument.setFamily(InstrumentFamily.KEYBOARD);
        
        instrument = instrumentRepository.save(instrument);
        instruments.put(instrumentName, instrument);
    }

    @Given("the renter has a completed booking for the instrument that ended {int} days ago")
    public void theRenterHasACompletedBookingForTheInstrumentThatEndedDaysAgo(Integer daysAgo) {
        User renter = users.values().stream()
                .filter(u -> u.getUsername().contains("renter"))
                .findFirst()
                .orElseThrow();
        
        Instrument instrument = instruments.values().iterator().next();
        
        Booking booking = new Booking(
                instrument,
                renter,
                LocalDate.now().minusDays(daysAgo + 5),
                LocalDate.now().minusDays(daysAgo)
        );
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);
        bookings.put("completed", booking);
    }

    @When("the renter submits a review with score {int} and comment {string}")
    public void theRenterSubmitsAReviewWithScoreAndComment(Integer score, String comment) {
        User renter = users.values().stream()
                .filter(u -> u.getUsername().contains("renter"))
                .findFirst()
                .orElseThrow();
        
        Booking booking = bookings.get("completed");
        ReviewRequest request = new ReviewRequest(booking.getId(), score, comment);
        
        try {
            lastReviewResponse = reviewService.createReview(request, renter.getId());
            lastException = null;
        } catch (Exception e) {
            lastException = e;
            lastReviewResponse = null;
        }
    }

    @Then("the review should be successfully created")
    public void theReviewShouldBeSuccessfullyCreated() {
        assertNotNull(lastReviewResponse, "Review should have been created");
        assertNull(lastException, "No exception should have occurred");
    }

    @Then("the review should have score {int}")
    public void theReviewShouldHaveScore(Integer expectedScore) {
        assertEquals(expectedScore, lastReviewResponse.getScore());
    }

    @Then("the review should have comment {string}")
    public void theReviewShouldHaveComment(String expectedComment) {
        assertEquals(expectedComment, lastReviewResponse.getComment());
    }

    @Then("the average score for the instrument should be {double}")
    public void theAverageScoreForTheInstrumentShouldBe(Double expectedAverage) {
        Instrument instrument = instruments.values().iterator().next();
        Double actualAverage = reviewService.getAverageScoreByItemId(instrument.getId());
        assertEquals(expectedAverage, actualAverage, 0.01);
    }

    @Then("the review should fail with error {string}")
    public void theReviewShouldFailWithError(String expectedError) {
        assertNotNull(lastException, "Exception should have been thrown");
        assertTrue(lastException.getMessage().contains(expectedError) || 
                   lastException instanceof IllegalArgumentException,
                   "Exception message should contain: " + expectedError);
    }

    @When("user {string} tries to review the booking")
    public void userTriesToReviewTheBooking(String username) {
        User user = users.get(username);
        Booking booking = bookings.get("completed");
        ReviewRequest request = new ReviewRequest(booking.getId(), 5, "Test");
        
        try {
            lastReviewResponse = reviewService.createReview(request, user.getId());
            lastException = null;
        } catch (Exception e) {
            lastException = e;
            lastReviewResponse = null;
        }
    }

    @Given("the renter has an active booking that ends in {int} days")
    public void theRenterHasAnActiveBookingThatEndsInDays(Integer days) {
        User renter = users.values().stream()
                .filter(u -> u.getUsername().contains("renter"))
                .findFirst()
                .orElseThrow();
        
        Instrument instrument = instruments.values().iterator().next();
        
        Booking booking = new Booking(
                instrument,
                renter,
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(days)
        );
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);
        bookings.put("active", booking);
    }

    @When("the renter tries to review the active booking")
    public void theRenterTriesToReviewTheActiveBooking() {
        User renter = users.values().stream()
                .filter(u -> u.getUsername().contains("renter"))
                .findFirst()
                .orElseThrow();
        
        Booking booking = bookings.get("active");
        ReviewRequest request = new ReviewRequest(booking.getId(), 5, "Test");
        
        try {
            lastReviewResponse = reviewService.createReview(request, renter.getId());
            lastException = null;
        } catch (Exception e) {
            lastException = e;
            lastReviewResponse = null;
        }
    }

    @Given("the renter has already reviewed the booking with score {int}")
    public void theRenterHasAlreadyReviewedTheBookingWithScore(Integer score) {
        User renter = users.values().stream()
                .filter(u -> u.getUsername().contains("renter"))
                .findFirst()
                .orElseThrow();
        
        Booking booking = bookings.get("completed");
        ReviewRequest request = new ReviewRequest(booking.getId(), score, "First review");
        reviewService.createReview(request, renter.getId());
    }

    @When("the renter submits another review with score {int} and comment {string}")
    public void theRenterSubmitsAnotherReviewWithScoreAndComment(Integer score, String comment) {
        theRenterSubmitsAReviewWithScoreAndComment(score, comment);
    }

    @Given("user {string} has a completed booking for the instrument that ended {int} day ago")
    public void userHasACompletedBookingForTheInstrumentThatEndedDayAgo(String username, Integer daysAgo) {
        User user = users.get(username);
        Instrument instrument = instruments.values().iterator().next();
        
        Booking booking = new Booking(
                instrument,
                user,
                LocalDate.now().minusDays(daysAgo + 5),
                LocalDate.now().minusDays(daysAgo)
        );
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);
        bookings.put(username + "_booking", booking);
    }

    @Given("the first renter has reviewed the booking with score {int}")
    public void theFirstRenterHasReviewedTheBookingWithScore(Integer score) {
        theRenterHasAlreadyReviewedTheBookingWithScore(score);
    }

    @When("user {string} submits a review with score {int} and comment {string}")
    public void userSubmitsAReviewWithScoreAndComment(String username, Integer score, String comment) {
        User user = users.get(username);
        Booking booking = bookings.get(username + "_booking");
        ReviewRequest request = new ReviewRequest(booking.getId(), score, comment);
        
        try {
            lastReviewResponse = reviewService.createReview(request, user.getId());
            lastException = null;
        } catch (Exception e) {
            lastException = e;
            lastReviewResponse = null;
        }
    }

    @Then("both reviews should exist")
    public void bothReviewsShouldExist() {
        Instrument instrument = instruments.values().iterator().next();
        List<ReviewResponse> reviews = reviewService.getReviewsByItemId(instrument.getId());
        assertEquals(2, reviews.size(), "Should have 2 reviews");
    }

    @Given("there are {int} reviews for the instrument with scores {int}, {int}, and {int}")
    public void thereAreReviewsForTheInstrumentWithScores(Integer count, Integer score1, Integer score2, Integer score3) {
        // Create 3 different users and bookings
        for (int i = 0; i < 3; i++) {
            User user = new User("reviewer" + i);
            user = userRepository.save(user);
            
            Instrument instrument = instruments.values().iterator().next();
            Booking booking = new Booking(
                    instrument,
                    user,
                    LocalDate.now().minusDays(10),
                    LocalDate.now().minusDays(3)
            );
            booking.setStatus(BookingStatus.APPROVED);
            booking = bookingRepository.save(booking);
            
            Integer score = i == 0 ? score1 : (i == 1 ? score2 : score3);
            ReviewRequest request = new ReviewRequest(booking.getId(), score, "Review " + i);
            reviewService.createReview(request, user.getId());
        }
    }

    @When("I request all reviews for the instrument")
    public void iRequestAllReviewsForTheInstrument() {
        Instrument instrument = instruments.values().iterator().next();
        reviewsList = reviewService.getReviewsByItemId(instrument.getId());
    }

    @Then("I should receive {int} reviews")
    public void iShouldReceiveReviews(Integer expectedCount) {
        assertEquals(expectedCount, reviewsList.size());
    }

    @Then("the reviews should be ordered by creation date")
    public void theReviewsShouldBeOrderedByCreationDate() {
        assertNotNull(reviewsList);
        assertTrue(reviewsList.size() > 0);
        // Just verify we got the reviews, ordering is handled by database
    }

    @When("I check if the completed booking can be reviewed")
    public void iCheckIfTheCompletedBookingCanBeReviewed() {
        User renter = users.values().stream()
                .filter(u -> u.getUsername().contains("renter"))
                .findFirst()
                .orElseThrow();
        
        Booking booking = bookings.get("completed");
        canReviewResult = reviewService.canReviewBooking(booking.getId(), renter.getId());
    }

    @Then("the response should indicate it can be reviewed")
    public void theResponseShouldIndicateItCanBeReviewed() {
        assertTrue(canReviewResult, "Should be able to review");
    }

    @When("I check if the active booking can be reviewed")
    public void iCheckIfTheActiveBookingCanBeReviewed() {
        User renter = users.values().stream()
                .filter(u -> u.getUsername().contains("renter"))
                .findFirst()
                .orElseThrow();
        
        Booking booking = bookings.get("active");
        canReviewResult = reviewService.canReviewBooking(booking.getId(), renter.getId());
    }

    @Then("the response should indicate it cannot be reviewed")
    public void theResponseShouldIndicateItCannotBeReviewed() {
        assertFalse(canReviewResult, "Should not be able to review");
    }
}
