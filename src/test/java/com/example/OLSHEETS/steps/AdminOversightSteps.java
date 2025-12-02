package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.Booking;
import com.example.OLSHEETS.data.BookingRepository;
import com.example.OLSHEETS.data.BookingStatus;
import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.repository.ItemRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class AdminOversightSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private List<Booking> adminBookingResults;
    private Map<String, Long> statisticsResults;
    private Long activityCount;
    private Double revenueAmount;
    private Map<Long, Instrument> instrumentMap = new HashMap<>();

    @Given("the following instruments exist for admin oversight:")
    public void theFollowingInstrumentsExistForAdminOversight(DataTable dataTable) {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        instrumentMap.clear();

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Instrument instrument = new Instrument();
            instrument.setName(row.get("name"));
            instrument.setType(InstrumentType.valueOf(row.get("type").toUpperCase()));
            instrument.setFamily(InstrumentFamily.valueOf(row.get("family").toUpperCase()));
            instrument.setAge(Integer.parseInt(row.get("age")));
            instrument.setPrice(Double.parseDouble(row.get("price")));
            instrument.setDescription(row.get("description"));
            instrument.setOwnerId(Integer.parseInt(row.get("ownerId")));

            instrument = itemRepository.save(instrument);
            instrumentMap.put(instrument.getId(), instrument);
        }
    }

    @Given("the following bookings exist:")
    public void theFollowingBookingsExist(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String itemName = row.get("itemName");
            Instrument instrument = instrumentMap.values().stream()
                .filter(i -> i.getName().equals(itemName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Instrument not found: " + itemName));

            Booking booking = new Booking();
            booking.setItem(instrument);
            booking.setRenterId(Long.parseLong(row.get("renterId")));
            booking.setStartDate(LocalDate.parse(row.get("startDate")));
            booking.setEndDate(LocalDate.parse(row.get("endDate")));
            booking.setStatus(BookingStatus.valueOf(row.get("status").toUpperCase()));

            bookingRepository.save(booking);
        }
    }

    @When("the admin requests all bookings")
    public void theAdminRequestsAllBookings() {
        String url = "http://localhost:" + port + "/api/admin/bookings";
        ResponseEntity<List<Booking>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Booking>>() {}
        );
        adminBookingResults = response.getBody();
    }

    @When("the admin filters bookings by status {string}")
    public void theAdminFiltersBookingsByStatus(String status) {
        String url = "http://localhost:" + port + "/api/admin/bookings/status/" + status;
        ResponseEntity<List<Booking>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Booking>>() {}
        );
        adminBookingResults = response.getBody();
    }

    @When("the admin views bookings for renter {long}")
    public void theAdminViewsBookingsForRenter(Long renterId) {
        String url = "http://localhost:" + port + "/api/admin/bookings/renter/" + renterId;
        ResponseEntity<List<Booking>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Booking>>() {}
        );
        adminBookingResults = response.getBody();
    }

    @When("the admin cancels the booking for {string}")
    public void theAdminCancelsTheBookingFor(String itemName) {
        Booking booking = bookingRepository.findAll().stream()
            .filter(b -> b.getItem().getName().equals(itemName))
            .findFirst()
            .orElseThrow();

        String url = "http://localhost:" + port + "/api/admin/bookings/" + booking.getId() + "/cancel";
        ResponseEntity<Booking> response = restTemplate.exchange(
            url,
            HttpMethod.PUT,
            null,
            Booking.class
        );
        adminBookingResults = List.of(response.getBody());
    }

    @When("the admin requests booking statistics")
    public void theAdminRequestsBookingStatistics() {
        String url = "http://localhost:" + port + "/api/admin/statistics/bookings";
        ResponseEntity<Map<String, Long>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Long>>() {}
        );
        statisticsResults = response.getBody();
    }

    @When("the admin checks activity for renter {long}")
    public void theAdminChecksActivityForRenter(Long renterId) {
        String url = "http://localhost:" + port + "/api/admin/activity/renter/" + renterId;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        activityCount = ((Number) response.getBody().get("bookingCount")).longValue();
    }

    @When("the admin checks activity for owner {int}")
    public void theAdminChecksActivityForOwner(int ownerId) {
        String url = "http://localhost:" + port + "/api/admin/activity/owner/" + ownerId;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        activityCount = ((Number) response.getBody().get("bookingCount")).longValue();
    }

    @When("the admin checks revenue for owner {int}")
    public void theAdminChecksRevenueForOwner(int ownerId) {
        String url = "http://localhost:" + port + "/api/admin/revenue/owner/" + ownerId;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        revenueAmount = ((Number) response.getBody().get("revenue")).doubleValue();
    }

    @When("the admin requests total revenue")
    public void theAdminRequestsTotalRevenue() {
        String url = "http://localhost:" + port + "/api/admin/revenue/total";
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        revenueAmount = ((Number) response.getBody().get("totalRevenue")).doubleValue();
    }

    @Then("the admin should see {int} bookings")
    public void theAdminShouldSeeBookings(int count) {
        assertNotNull(adminBookingResults);
        assertEquals(count, adminBookingResults.size());
    }

    @Then("the bookings should include all statuses")
    public void theBookingsShouldIncludeAllStatuses() {
        assertNotNull(adminBookingResults);
        assertTrue(adminBookingResults.size() > 0);
    }

    @Then("all bookings should have status {string}")
    public void allBookingsShouldHaveStatus(String status) {
        assertNotNull(adminBookingResults);
        BookingStatus expectedStatus = BookingStatus.valueOf(status.toUpperCase());
        for (Booking booking : adminBookingResults) {
            assertEquals(expectedStatus, booking.getStatus());
        }
    }

    @Then("all bookings should be for renter {long}")
    public void allBookingsShouldBeForRenter(Long renterId) {
        assertNotNull(adminBookingResults);
        for (Booking booking : adminBookingResults) {
            assertEquals(renterId, booking.getRenterId());
        }
    }

    @Then("the booking status should be {string}")
    public void theBookingStatusShouldBe(String status) {
        assertNotNull(adminBookingResults);
        assertEquals(1, adminBookingResults.size());
        assertEquals(BookingStatus.valueOf(status.toUpperCase()), adminBookingResults.get(0).getStatus());
    }

    @Then("the statistics should show:")
    public void theStatisticsShouldShow(DataTable dataTable) {
        assertNotNull(statisticsResults);
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String metric = row.get("metric");
            Long expectedValue = Long.parseLong(row.get("value"));
            assertEquals(expectedValue, statisticsResults.get(metric), "Mismatch for metric: " + metric);
        }
    }

    @Then("the renter should have {int} bookings")
    public void theRenterShouldHaveBookings(int count) {
        assertNotNull(activityCount);
        assertEquals(Long.valueOf(count), activityCount);
    }

    @Then("the owner should have {int} bookings")
    public void theOwnerShouldHaveBookings(int count) {
        assertNotNull(activityCount);
        assertEquals(Long.valueOf(count), activityCount);
    }

    @Then("the owner revenue should be {double}")
    public void theOwnerRevenueShouldBe(double expectedRevenue) {
        assertNotNull(revenueAmount);
        assertEquals(expectedRevenue, revenueAmount, 0.01);
    }

    @Then("the total system revenue should be {double}")
    public void theTotalSystemRevenueShouldBe(double expectedRevenue) {
        assertNotNull(revenueAmount);
        assertEquals(expectedRevenue, revenueAmount, 0.01);
    }
}