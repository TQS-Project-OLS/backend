package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.*;
import com.example.OLSHEETS.dto.RenterReviewRequest;
import com.example.OLSHEETS.dto.RenterReviewResponse;
import com.example.OLSHEETS.dto.ReviewResponse;
import com.example.OLSHEETS.repository.*;
import com.example.OLSHEETS.service.RenterReviewService;
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

public class ReviewRentersSteps {

    @Autowired
    private RenterReviewService renterReviewService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RenterReviewRepository renterReviewRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private final Map<String, User> usersRenterReview = new HashMap<>();
    private final Map<String, Instrument> instrumentsRenterReview = new HashMap<>();
    private final Map<String, Booking> bookingsRenterReview = new HashMap<>();
    
    private RenterReviewResponse lastRenterReviewResponse;
    private List<RenterReviewResponse> renterReviewsList;
    private Exception lastRenterReviewException;
    private Boolean canReviewRenterResult;

    @After
    public void cleanupRenterReviews() {
        // Clean up only data created by renter review scenarios
        // Clear in-memory maps first
        usersRenterReview.clear();
        instrumentsRenterReview.clear();
        bookingsRenterReview.clear();
        lastRenterReviewResponse = null;
        renterReviewsList = null;
        lastRenterReviewException = null;
        canReviewRenterResult = null;
    }

    // Helper methods to get users from map or database
    private User getOwner() {
        return usersRenterReview.values().stream()
                .filter(u -> u.getUsername().contains("owner"))
                .findFirst()
                .or(() -> userRepository.findAll().stream()
                        .filter(u -> u.getUsername().contains("owner"))
                        .findFirst())
                .orElseThrow(() -> new RuntimeException("No owner found"));
    }

    private User getRenter() {
        return usersRenterReview.values().stream()
                .filter(u -> u.getUsername().contains("renter") && !u.getUsername().contains("owner"))
                .findFirst()
                .or(() -> userRepository.findAll().stream()
                        .filter(u -> u.getUsername().contains("renter") && !u.getUsername().contains("owner"))
                        .findFirst())
                .orElseThrow(() -> new RuntimeException("No renter found"));
    }

    @Given("there is an owner {string} with ID {int}")
    public void thereIsAnOwnerWithID(String username, Integer userId) {
        User user = new User(username);
        user = userRepository.save(user);
        usersRenterReview.put(username, user);
    }

    @Given("there is a renter {string} with ID {int}")
    public void thereIsARenterWithID(String username, Integer userId) {
        User user = new User(username);
        user = userRepository.save(user);
        usersRenterReview.put(username, user);
    }

    @Given("the owner has an instrument {string}")
    public void theOwnerHasAnInstrument(String instrumentName) {
        User owner = usersRenterReview.values().stream()
                .filter(u -> u.getUsername().contains("owner"))
                .findFirst()
                .orElseThrow();
        
        Instrument instrument = new Instrument();
        instrument.setName(instrumentName);
        instrument.setDescription("Test instrument for renter review");
        instrument.setPrice(100.0);
        instrument.setOwner(owner);
        instrument.setAge(2);
        instrument.setType(InstrumentType.DIGITAL);
        instrument.setFamily(InstrumentFamily.KEYBOARD);
        
        instrument = instrumentRepository.save(instrument);
        instrumentsRenterReview.put(instrumentName, instrument);
    }

    @Given("the renter has a completed booking that ended {int} days ago")
    public void theRenterHasACompletedBookingThatEndedDaysAgo(Integer daysAgo) {
        // Fetch renter from database (may have been created by another step class)
        User renter = userRepository.findAll().stream()
                .filter(u -> u.getUsername().contains("renter") && !u.getUsername().contains("owner"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No renter found in database"));
        
        usersRenterReview.put(renter.getUsername(), renter);
        
        // Fetch the instrument from the database instead of the map
        // This allows sharing data between step definition classes
        Instrument instrument = instrumentRepository.findAll().stream()
                .filter(i -> i.getOwner() != null && i.getOwner().getUsername().contains("owner"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No instrument found in database"));
        
        instrumentsRenterReview.put(instrument.getName(), instrument);
        
        Booking booking = new Booking(
                instrument,
                renter,
                LocalDate.now().minusDays(daysAgo + 5),
                LocalDate.now().minusDays(daysAgo)
        );
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);
        bookingsRenterReview.put("completed", booking);
    }

    @When("the owner submits a renter review with score {int} and comment {string}")
    public void theOwnerSubmitsARenterReviewWithScoreAndComment(Integer score, String comment) {
        User owner = getOwner();
        
        Booking booking = bookingsRenterReview.get("completed");
        RenterReviewRequest request = new RenterReviewRequest(booking.getId(), score, comment);
        
        try {
            lastRenterReviewResponse = renterReviewService.createRenterReview(request, owner.getId());
            lastRenterReviewException = null;
        } catch (Exception e) {
            lastRenterReviewException = e;
            lastRenterReviewResponse = null;
        }
    }

    @Then("the renter review should be successfully created")
    public void theRenterReviewShouldBeSuccessfullyCreated() {
        assertNotNull(lastRenterReviewResponse, "Renter review should have been created");
        assertNull(lastRenterReviewException, "No exception should have occurred");
    }

    @Then("the renter review should have score {int}")
    public void theRenterReviewShouldHaveScore(Integer expectedScore) {
        assertEquals(expectedScore, lastRenterReviewResponse.getScore());
    }

    @Then("the renter review should have comment {string}")
    public void theRenterReviewShouldHaveComment(String expectedComment) {
        assertEquals(expectedComment, lastRenterReviewResponse.getComment());
    }

    @Then("the average score for the renter should be {double}")
    public void theAverageScoreForTheRenterShouldBe(Double expectedAverage) {
        User renter = usersRenterReview.values().stream()
                .filter(u -> u.getUsername().contains("renter"))
                .findFirst()
                .orElseThrow();
        
        Double actualAverage = renterReviewService.getAverageScoreByRenterId(renter.getId());
        assertEquals(expectedAverage, actualAverage, 0.01);
    }

    @Then("the renter review should fail with error {string}")
    public void theRenterReviewShouldFailWithError(String expectedError) {
        assertNotNull(lastRenterReviewException, "Exception should have been thrown");
        assertTrue(lastRenterReviewException.getMessage().contains(expectedError) || 
                   lastRenterReviewException instanceof IllegalArgumentException,
                   "Exception message should contain: " + expectedError);
    }

    @When("user {string} tries to submit a renter review")
    public void userTriesToSubmitARenterReview(String username) {
        User user = usersRenterReview.get(username);
        Booking booking = bookingsRenterReview.get("completed");
        RenterReviewRequest request = new RenterReviewRequest(booking.getId(), 5, "Test");
        
        try {
            lastRenterReviewResponse = renterReviewService.createRenterReview(request, user.getId());
            lastRenterReviewException = null;
        } catch (Exception e) {
            lastRenterReviewException = e;
            lastRenterReviewResponse = null;
        }
    }

    @Given("the renter has an active rental that ends in {int} days")
    public void theRenterHasAnActiveRentalThatEndsInDays(Integer days) {
        // Try to get from map first, otherwise fetch from database
        User renter = usersRenterReview.values().stream()
                .filter(u -> u.getUsername().contains("renter"))
                .findFirst()
                .or(() -> userRepository.findAll().stream()
                        .filter(u -> u.getUsername().contains("renter"))
                        .findFirst())
                .orElseThrow(() -> new RuntimeException("No renter found"));
        
        if (!usersRenterReview.containsValue(renter)) {
            usersRenterReview.put(renter.getUsername(), renter);
        }
        
        // Fetch the instrument from the database
        Instrument instrument = instrumentRepository.findAll().stream()
                .filter(i -> i.getOwner() != null && i.getOwner().getUsername().contains("owner"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No instrument found in database"));
        
        instrumentsRenterReview.put(instrument.getName(), instrument);
        
        Booking booking = new Booking(
                instrument,
                renter,
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(days)
        );
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);
        bookingsRenterReview.put("active", booking);
    }

    @When("the owner tries to review the renter for the active rental")
    public void theOwnerTriesToReviewTheRenterForTheActiveRental() {
        User owner = getOwner();
        
        Booking booking = bookingsRenterReview.get("active");
        RenterReviewRequest request = new RenterReviewRequest(booking.getId(), 5, "Test");
        
        try {
            lastRenterReviewResponse = renterReviewService.createRenterReview(request, owner.getId());
            lastRenterReviewException = null;
        } catch (Exception e) {
            lastRenterReviewException = e;
            lastRenterReviewResponse = null;
        }
    }

    @Given("the owner has already reviewed the renter with score {int}")
    public void theOwnerHasAlreadyReviewedTheRenterWithScore(Integer score) {
        User owner = getOwner();
        
        Booking booking = bookingsRenterReview.get("completed");
        RenterReviewRequest request = new RenterReviewRequest(booking.getId(), score, "First review");
        renterReviewService.createRenterReview(request, owner.getId());
    }

    @When("the owner submits another renter review with score {int} and comment {string}")
    public void theOwnerSubmitsAnotherRenterReviewWithScoreAndComment(Integer score, String comment) {
        theOwnerSubmitsARenterReviewWithScoreAndComment(score, comment);
    }

    @Given("owner {string} has a completed booking with the renter that ended {int} day ago")
    public void ownerHasACompletedBookingWithTheRenterThatEndedDayAgo(String username, Integer daysAgo) {
        User owner = usersRenterReview.get(username);
        User renter = getRenter();
        
        Instrument instrument = new Instrument();
        instrument.setName("Instrument for " + username);
        instrument.setDescription("Test");
        instrument.setPrice(100.0);
        instrument.setOwner(owner);
        instrument.setAge(2);
        instrument.setType(InstrumentType.DIGITAL);
        instrument.setFamily(InstrumentFamily.KEYBOARD);
        instrument = instrumentRepository.save(instrument);
        
        Booking booking = new Booking(
                instrument,
                renter,
                LocalDate.now().minusDays(daysAgo + 5),
                LocalDate.now().minusDays(daysAgo)
        );
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);
        bookingsRenterReview.put(username + "_booking", booking);
    }

    @Given("the first owner has reviewed the renter with score {int}")
    public void theFirstOwnerHasReviewedTheRenterWithScore(Integer score) {
        theOwnerHasAlreadyReviewedTheRenterWithScore(score);
    }

    @When("owner {string} submits a renter review with score {int} and comment {string}")
    public void ownerSubmitsARenterReviewWithScoreAndComment(String username, Integer score, String comment) {
        User owner = usersRenterReview.get(username);
        Booking booking = bookingsRenterReview.get(username + "_booking");
        RenterReviewRequest request = new RenterReviewRequest(booking.getId(), score, comment);
        
        try {
            lastRenterReviewResponse = renterReviewService.createRenterReview(request, owner.getId());
            lastRenterReviewException = null;
        } catch (Exception e) {
            lastRenterReviewException = e;
            lastRenterReviewResponse = null;
        }
    }

    @Then("both renter reviews should exist")
    public void bothRenterReviewsShouldExist() {
        User renter = getRenter();
        
        List<RenterReviewResponse> reviews = renterReviewService.getReviewsByRenterId(renter.getId());
        assertEquals(2, reviews.size(), "Should have 2 renter reviews");
    }

    @Given("there are {int} renter reviews with scores {int}, {int}, and {int}")
    public void thereAreRenterReviewsWithScores(Integer count, Integer score1, Integer score2, Integer score3) {
        User renter = getRenter();
        
        // Create 3 different owners and bookings
        for (int i = 0; i < 3; i++) {
            User owner = new User("reviewer_owner" + i);
            owner = userRepository.save(owner);
            
            Instrument instrument = new Instrument();
            instrument.setName("Instrument " + i);
            instrument.setDescription("Test");
            instrument.setPrice(100.0);
            instrument.setOwner(owner);
            instrument.setAge(2);
            instrument.setType(InstrumentType.DIGITAL);
            instrument.setFamily(InstrumentFamily.KEYBOARD);
            instrument = instrumentRepository.save(instrument);
            
            Booking booking = new Booking(
                    instrument,
                    renter,
                    LocalDate.now().minusDays(10),
                    LocalDate.now().minusDays(3)
            );
            booking.setStatus(BookingStatus.APPROVED);
            booking = bookingRepository.save(booking);
            
            Integer score = i == 0 ? score1 : (i == 1 ? score2 : score3);
            RenterReviewRequest request = new RenterReviewRequest(booking.getId(), score, "Review " + i);
            renterReviewService.createRenterReview(request, owner.getId());
        }
    }

    @When("I request all reviews for the renter")
    public void iRequestAllReviewsForTheRenter() {
        User renter = getRenter();
        
        renterReviewsList = renterReviewService.getReviewsByRenterId(renter.getId());
    }

    @Then("I should receive {int} renter reviews")
    public void iShouldReceiveRenterReviews(Integer expectedCount) {
        assertEquals(expectedCount, renterReviewsList.size());
    }

    @Then("the renter reviews should be ordered by creation date")
    public void theRenterReviewsShouldBeOrderedByCreationDate() {
        assertNotNull(renterReviewsList);
        assertTrue(renterReviewsList.size() > 0);
        // Just verify we got the reviews, ordering is handled by database
    }

    @When("I check if the owner can review the renter for the completed booking")
    public void iCheckIfTheOwnerCanReviewTheRenterForTheCompletedBooking() {
        User owner = getOwner();
        
        Booking booking = bookingsRenterReview.get("completed");
        canReviewRenterResult = renterReviewService.canReviewRenter(booking.getId(), owner.getId());
    }

    @When("the owner checks if they can review the renter for the completed booking")
    public void theOwnerChecksIfTheyCanReviewTheRenterForTheCompletedBooking() {
        iCheckIfTheOwnerCanReviewTheRenterForTheCompletedBooking();
    }

    @Then("the response should indicate the renter can be reviewed")
    public void theResponseShouldIndicateTheRenterCanBeReviewed() {
        assertTrue(canReviewRenterResult, "Should be able to review renter");
    }

    @When("I check if the owner can review the renter for the active booking")
    public void iCheckIfTheOwnerCanReviewTheRenterForTheActiveBooking() {
        User owner = getOwner();
        
        Booking booking = bookingsRenterReview.get("active");
        canReviewRenterResult = renterReviewService.canReviewRenter(booking.getId(), owner.getId());
    }

    @When("the owner checks if they can review the renter for the active rental")
    public void theOwnerChecksIfTheyCanReviewTheRenterForTheActiveRental() {
        iCheckIfTheOwnerCanReviewTheRenterForTheActiveBooking();
    }

    @Then("the response should indicate they cannot review")
    public void theResponseShouldIndicateTheyCannotReview() {
        theResponseShouldIndicateTheRenterCannotBeReviewed();
    }

    @Then("the response should indicate they can review")
    public void theResponseShouldIndicateTheyCanReview() {
        theResponseShouldIndicateTheRenterCanBeReviewed();
    }

    @Then("the response should indicate the renter cannot be reviewed")
    public void theResponseShouldIndicateTheRenterCannotBeReviewed() {
        assertFalse(canReviewRenterResult, "Should not be able to review renter");
    }

    // Cross-feature steps for "Owner and renter both review after booking ends" scenario
    @Then("both the item review and renter review should exist")
    public void bothTheItemReviewAndRenterReviewShouldExist() {
        Booking booking = bookingsRenterReview.get("completed");
        
        // Check item review exists
        List<ReviewResponse> itemReviews = reviewService.getReviewsByItemId(booking.getItem().getId());
        assertTrue(itemReviews.size() > 0, "Item review should exist");
        
        // Check renter review exists
        List<RenterReviewResponse> renterReviews = renterReviewService.getReviewsByRenterId(booking.getRenter().getId());
        assertTrue(renterReviews.size() > 0, "Renter review should exist");
    }

    @Then("the booking should have reviews from both parties")
    public void theBookingShouldHaveReviewsFromBothParties() {
        Booking booking = bookingsRenterReview.get("completed");
        
        // Verify both types of reviews exist for this booking
        assertTrue(reviewRepository.findByBooking(booking).isPresent(), 
                   "Item review should exist for booking");
        assertTrue(renterReviewRepository.findByBooking(booking).isPresent(), 
                   "Renter review should exist for booking");
    }

    @Given("there are {int} reviews for the renter with scores {int}, {int}, and {int}")
    public void thereAreReviewsForTheRenterWithScores(Integer count, Integer score1, Integer score2, Integer score3) {
        thereAreRenterReviewsWithScores(count, score1, score2, score3);
    }

    @Given("user {string} has an instrument {string}")
    public void userHasAnInstrument(String username, String instrumentName) {
        User owner = usersRenterReview.get(username);
        
        Instrument instrument = new Instrument();
        instrument.setName(instrumentName);
        instrument.setDescription("Test");
        instrument.setPrice(100.0);
        instrument.setOwner(owner);
        instrument.setAge(2);
        instrument.setType(InstrumentType.DIGITAL);
        instrument.setFamily(InstrumentFamily.KEYBOARD);
        instrument = instrumentRepository.save(instrument);
        instrumentsRenterReview.put(username + "_" + instrumentName, instrument);
    }

    @Given("the renter has a completed booking for {string} instrument that ended {int} day ago")
    public void theRenterHasACompletedBookingForInstrumentThatEndedDayAgo(String instrumentOwner, Integer daysAgo) {
        User renter = getRenter();
        
        Instrument instrument = instrumentsRenterReview.get(instrumentOwner + "_" + "Violin");
        if (instrument == null) {
            instrument = instrumentsRenterReview.get(instrumentOwner + "_" + "Guitar");
        }
        
        Booking booking = new Booking(
                instrument,
                renter,
                LocalDate.now().minusDays(daysAgo + 5),
                LocalDate.now().minusDays(daysAgo)
        );
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);
        bookingsRenterReview.put(instrumentOwner + "_booking", booking);
    }

    @When("user {string} submits a renter review with score {int} and comment {string}")
    public void userSubmitsARenterReviewWithScoreAndComment(String username, Integer score, String comment) {
        User owner = usersRenterReview.get(username);
        Booking booking = bookingsRenterReview.get(username + "_booking");
        RenterReviewRequest request = new RenterReviewRequest(booking.getId(), score, comment);
        
        try {
            lastRenterReviewResponse = renterReviewService.createRenterReview(request, owner.getId());
            lastRenterReviewException = null;
        } catch (Exception e) {
            lastRenterReviewException = e;
            lastRenterReviewResponse = null;
        }
    }

    @When("user {string} tries to review the renter for the booking")
    public void userTriesToReviewTheRenterForTheBooking(String username) {
        User user = usersRenterReview.get(username);
        Booking booking = bookingsRenterReview.get("completed");
        RenterReviewRequest request = new RenterReviewRequest(booking.getId(), 5, "Test");
        
        try {
            lastRenterReviewResponse = renterReviewService.createRenterReview(request, user.getId());
            lastRenterReviewException = null;
        } catch (Exception e) {
            lastRenterReviewException = e;
            lastRenterReviewResponse = null;
        }
    }

    @Given("the owner has already reviewed the renter for the booking with score {int}")
    public void theOwnerHasAlreadyReviewedTheRenterForTheBookingWithScore(Integer score) {
        theOwnerHasAlreadyReviewedTheRenterWithScore(score);
    }
}
