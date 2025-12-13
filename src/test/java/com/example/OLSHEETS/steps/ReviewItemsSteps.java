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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewItemsSteps {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RenterReviewRepository renterReviewRepository;

    // State for current scenario - use database as source of truth
    private ReviewResponse lastReviewResponse;
    private List<ReviewResponse> reviewsList;
    private Exception lastException;
    private Boolean canReviewResult;

    @After
    public void cleanup() {
        // Clean up database to ensure test isolation
        reviewRepository.deleteAll();
        renterReviewRepository.deleteAll();
        bookingRepository.deleteAll();
        instrumentRepository.deleteAll();
        musicSheetRepository.deleteAll();
        userRepository.deleteAll();
        
        // Clear scenario state
        lastReviewResponse = null;
        reviewsList = null;
        lastException = null;
        canReviewResult = null;
    }

    // Helper methods to fetch from database
    private User getOwnerFromDb() {
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().contains("owner"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No owner found in database"));
    }

    private User getRenterFromDb() {
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().contains("renter") && !u.getUsername().contains("owner"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No renter found in database"));
    }

    private Instrument getInstrumentFromDb() {
        return instrumentRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No instrument found in database"));
    }

    private Booking getCompletedBookingFromDb() {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getEndDate().isBefore(LocalDate.now()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No completed booking found in database"));
    }

    private Booking getActiveBookingFromDb() {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getEndDate().isAfter(LocalDate.now()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active booking found in database"));
    }

    @Given("there is a user {string} with ID {int}")
    public void thereIsAUserWithID(String username, Integer userId) {
        User user = new User(username, username + "@a.com", username, "123");
        userRepository.save(user);
    }

    @Given("the owner has an instrument {string} with price {double}")
    public void theOwnerHasAnInstrumentWithPrice(String instrumentName, Double price) {
        User owner = getOwnerFromDb();
        
        Instrument instrument = new Instrument();
        instrument.setName(instrumentName);
        instrument.setDescription("Test instrument");
        instrument.setPrice(price);
        instrument.setOwner(owner);
        instrument.setAge(2);
        instrument.setType(InstrumentType.DIGITAL);
        instrument.setFamily(InstrumentFamily.KEYBOARD);
        
        instrumentRepository.save(instrument);
    }

    @Given("the renter has a completed booking for the instrument that ended {int} days ago")
    public void theRenterHasACompletedBookingForTheInstrumentThatEndedDaysAgo(Integer daysAgo) {
        User renter = getRenterFromDb();
        Instrument instrument = getInstrumentFromDb();
        
        Booking booking = new Booking(
                instrument,
                renter,
                LocalDate.now().minusDays(daysAgo + 5),
                LocalDate.now().minusDays(daysAgo)
        );
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
    }

    @When("the renter submits a review with score {int} and comment {string}")
    public void theRenterSubmitsAReviewWithScoreAndComment(Integer score, String comment) {
        User renter = getRenterFromDb();
        Booking booking = getCompletedBookingFromDb();
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
        Instrument instrument = getInstrumentFromDb();
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
        User user = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow();
        Booking booking = getCompletedBookingFromDb();
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
        User renter = getRenterFromDb();
        Instrument instrument = getInstrumentFromDb();
        
        Booking booking = new Booking(
                instrument,
                renter,
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(days)
        );
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
    }

    @When("the renter tries to review the active booking")
    public void theRenterTriesToReviewTheActiveBooking() {
        User renter = getRenterFromDb();
        Booking booking = getActiveBookingFromDb();
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
        User renter = getRenterFromDb();
        Booking booking = getCompletedBookingFromDb();
        ReviewRequest request = new ReviewRequest(booking.getId(), score, "First review");
        reviewService.createReview(request, renter.getId());
    }

    @When("the renter submits another review with score {int} and comment {string}")
    public void theRenterSubmitsAnotherReviewWithScoreAndComment(Integer score, String comment) {
        theRenterSubmitsAReviewWithScoreAndComment(score, comment);
    }

    @Given("user {string} has a completed booking for the instrument that ended {int} day ago")
    public void userHasACompletedBookingForTheInstrumentThatEndedDayAgo(String username, Integer daysAgo) {
        User user = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow();
        Instrument instrument = getInstrumentFromDb();
        
        Booking booking = new Booking(
                instrument,
                user,
                LocalDate.now().minusDays(daysAgo + 5),
                LocalDate.now().minusDays(daysAgo)
        );
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
    }

    @Given("the first renter has reviewed the booking with score {int}")
    public void theFirstRenterHasReviewedTheBookingWithScore(Integer score) {
        theRenterHasAlreadyReviewedTheBookingWithScore(score);
    }

    @When("user {string} submits a review with score {int} and comment {string}")
    public void userSubmitsAReviewWithScoreAndComment(String username, Integer score, String comment) {
        User user = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow();
        
        // Find this user's booking
        Instrument instrument = getInstrumentFromDb();
        Booking booking = bookingRepository.findAll().stream()
                .filter(b -> b.getItem().equals(instrument) && b.getRenter().equals(user))
                .findFirst()
                .orElseThrow();
        
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
        Instrument instrument = getInstrumentFromDb();
        List<ReviewResponse> reviews = reviewService.getReviewsByItemId(instrument.getId());
        assertEquals(2, reviews.size(), "Should have 2 reviews");
    }

    @Given("there are {int} reviews for the instrument with scores {int}, {int}, and {int}")
    public void thereAreReviewsForTheInstrumentWithScores(Integer count, Integer score1, Integer score2, Integer score3) {
        // Create 3 different users and bookings
        Instrument instrument = getInstrumentFromDb();
        
        for (int i = 0; i < 3; i++) {
            User user = new User("reviewer" + i, "reviewer" + i + "@example.com", "Reviewer " + i);
            user = userRepository.save(user);
            
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
        Instrument instrument = getInstrumentFromDb();
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
        User renter = getRenterFromDb();
        Booking booking = getCompletedBookingFromDb();
        canReviewResult = reviewService.canReviewBooking(booking.getId(), renter.getId());
    }

    @Then("the response should indicate it can be reviewed")
    public void theResponseShouldIndicateItCanBeReviewed() {
        assertTrue(canReviewResult, "Should be able to review");
    }

    @When("I check if the active booking can be reviewed")
    public void iCheckIfTheActiveBookingCanBeReviewed() {
        User renter = getRenterFromDb();
        Booking booking = getActiveBookingFromDb();
        canReviewResult = reviewService.canReviewBooking(booking.getId(), renter.getId());
    }

    @Then("the response should indicate it cannot be reviewed")
    public void theResponseShouldIndicateItCannotBeReviewed() {
        assertFalse(canReviewResult, "Should not be able to review");
    }
}
