package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.*;
import com.example.OLSHEETS.repository.AvailabilityRepository;
import com.example.OLSHEETS.repository.InstrumentRepository;
import com.example.OLSHEETS.service.AvailabilityService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ManageAvailabilitySteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private AvailabilityService availabilityService;

    private ResponseEntity<?> lastAvailabilityResponse;
    private Availability lastCreatedAvailability;
    private List<Availability> retrievedAvailabilities;
    private Boolean availabilityCheckResult;
    private Long lastCreatedAvailabilityId;
    
    // Map to store instruments by their feature file ID
    private Map<Long, Instrument> instrumentMap = new HashMap<>();
    
    // Map to store availability IDs from feature file to actual database IDs
    private Map<Long, Long> availabilityIdMap = new HashMap<>();

    @Given("the following instruments exist for availability:")
    public void theFollowingInstrumentsExistForAvailability(DataTable dataTable) {
        availabilityRepository.deleteAll();
        instrumentRepository.deleteAll();
        instrumentMap.clear();

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Long featureFileId = Long.parseLong(row.get("id"));
            
            Instrument instrument = new Instrument();
            instrument.setName(row.get("name"));
            instrument.setType(InstrumentType.valueOf(row.get("type")));
            instrument.setFamily(InstrumentFamily.valueOf(row.get("family")));
            instrument.setAge(Integer.parseInt(row.get("age")));
            instrument.setPrice(Double.parseDouble(row.get("price")));
            instrument.setOwnerId(Integer.parseInt(row.get("ownerId")));
            instrument.setDescription("Test instrument");

            Instrument saved = instrumentRepository.save(instrument);
            instrumentMap.put(featureFileId, saved);
        }
    }

    @Given("an unavailability period exists for instrument {long} from {string} to {string} with reason {string}")
    public void anUnavailabilityPeriodExistsForInstrument(Long featureFileInstrumentId, String startDate, String endDate, String reason) {
        Long actualInstrumentId = instrumentMap.get(featureFileInstrumentId).getId();
        Availability availability = availabilityService.createUnavailability(
            actualInstrumentId,
            LocalDate.parse(startDate),
            LocalDate.parse(endDate),
            AvailabilityReason.valueOf(reason)
        );
        lastCreatedAvailabilityId = availability.getId();
    }

    @Given("an unavailability period with id {long} exists for instrument {long} from {string} to {string} with reason {string}")
    public void anUnavailabilityPeriodWithIdExistsForInstrument(Long featureFileAvailabilityId, Long featureFileInstrumentId, String startDate, String endDate, String reason) {
        Long actualInstrumentId = instrumentMap.get(featureFileInstrumentId).getId();
        Availability availability = availabilityService.createUnavailability(
            actualInstrumentId,
            LocalDate.parse(startDate),
            LocalDate.parse(endDate),
            AvailabilityReason.valueOf(reason)
        );
        lastCreatedAvailabilityId = availability.getId();
        // Store the mapping from feature file ID to actual database ID
        availabilityIdMap.put(featureFileAvailabilityId, availability.getId());
    }

    @When("I create an unavailability period for instrument {long} from {string} to {string} with reason {string}")
    public void iCreateAnUnavailabilityPeriodForInstrument(Long featureFileInstrumentId, String startDate, String endDate, String reason) {
        Long actualInstrumentId = instrumentMap.get(featureFileInstrumentId).getId();
        String url = "http://localhost:" + port + "/api/availability"
            + "?instrumentId=" + actualInstrumentId
            + "&startDate=" + startDate
            + "&endDate=" + endDate
            + "&reason=" + reason;

        try {
            lastAvailabilityResponse = restTemplate.postForEntity(url, null, Availability.class);
            
            if (lastAvailabilityResponse.getStatusCode().is2xxSuccessful()) {
                lastCreatedAvailability = (Availability) lastAvailabilityResponse.getBody();
                if (lastCreatedAvailability != null) {
                    lastCreatedAvailabilityId = lastCreatedAvailability.getId();
                }
            }
        } catch (Exception e) {
            lastAvailabilityResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @When("I try to create an unavailability period for instrument {long} from {string} to {string} with reason {string}")
    public void iTryToCreateAnUnavailabilityPeriodForInstrument(Long featureFileInstrumentId, String startDate, String endDate, String reason) {
        iCreateAnUnavailabilityPeriodForInstrument(featureFileInstrumentId, startDate, endDate, reason);
    }

    @When("I check availability for instrument {long} from {string} to {string}")
    public void iCheckAvailabilityForInstrument(Long featureFileInstrumentId, String startDate, String endDate) {
        Long actualInstrumentId = instrumentMap.get(featureFileInstrumentId).getId();
        String url = "http://localhost:" + port + "/api/availability/check?instrumentId=" + actualInstrumentId 
            + "&startDate=" + startDate + "&endDate=" + endDate;

        ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
        availabilityCheckResult = response.getBody();
    }

    @When("I retrieve all unavailability periods for instrument {long}")
    public void iRetrieveAllUnavailabilityPeriodsForInstrument(Long featureFileInstrumentId) {
        Long actualInstrumentId = instrumentMap.get(featureFileInstrumentId).getId();
        String url = "http://localhost:" + port + "/api/availability/instrument/" + actualInstrumentId;

        ResponseEntity<List<Availability>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Availability>>() {}
        );

        retrievedAvailabilities = response.getBody();
    }

    @When("I delete the unavailability period {long}")
    public void iDeleteTheUnavailabilityPeriod(Long featureFileAvailabilityId) {
        // Get the actual database ID from the mapping
        Long actualAvailabilityId = availabilityIdMap.getOrDefault(featureFileAvailabilityId, featureFileAvailabilityId);
        String url = "http://localhost:" + port + "/api/availability/" + actualAvailabilityId;

        restTemplate.delete(url);
        lastAvailabilityResponse = ResponseEntity.ok().build();
    }

    @Then("the unavailability should be created successfully")
    public void theUnavailabilityShouldBeCreatedSuccessfully() {
        assertNotNull(lastAvailabilityResponse);
        assertEquals(HttpStatus.CREATED, lastAvailabilityResponse.getStatusCode());
        assertNotNull(lastCreatedAvailability);
    }

    @Then("the unavailability creation should fail")
    public void theUnavailabilityCreationShouldFail() {
        assertNotNull(lastAvailabilityResponse);
        assertNotEquals(HttpStatus.CREATED, lastAvailabilityResponse.getStatusCode());
    }

    @Then("the instrument {long} should not be available from {string} to {string}")
    public void theInstrumentShouldNotBeAvailableFromTo(Long featureFileInstrumentId, String startDate, String endDate) {
        Long actualInstrumentId = instrumentMap.get(featureFileInstrumentId).getId();
        boolean isAvailable = availabilityService.isAvailable(
            actualInstrumentId,
            LocalDate.parse(startDate),
            LocalDate.parse(endDate)
        );
        assertFalse(isAvailable, "Expected instrument to be unavailable but it was available");
    }

    @Then("the instrument {long} should be available from {string} to {string}")
    public void theInstrumentShouldBeAvailableFromTo(Long featureFileInstrumentId, String startDate, String endDate) {
        Long actualInstrumentId = instrumentMap.get(featureFileInstrumentId).getId();
        boolean isAvailable = availabilityService.isAvailable(
            actualInstrumentId,
            LocalDate.parse(startDate),
            LocalDate.parse(endDate)
        );
        assertTrue(isAvailable, "Expected instrument to be available but it was not");
    }

    @Then("the instrument {long} should have {int} unavailability period(s)")
    public void theInstrumentShouldHaveUnavailabilityPeriods(Long featureFileInstrumentId, int expectedCount) {
        Long actualInstrumentId = instrumentMap.get(featureFileInstrumentId).getId();
        List<Availability> availabilities = availabilityService.getInstrumentAvailabilities(actualInstrumentId);
        assertEquals(expectedCount, availabilities.size());
    }

    @Then("the instrument should not be available")
    public void theInstrumentShouldNotBeAvailable() {
        assertNotNull(availabilityCheckResult);
        assertFalse(availabilityCheckResult, "Expected instrument to be unavailable but it was available");
    }

    @Then("the instrument should be available")
    public void theInstrumentShouldBeAvailable() {
        assertNotNull(availabilityCheckResult);
        assertTrue(availabilityCheckResult, "Expected instrument to be available but it was not");
    }

    @Then("I should receive {int} unavailability period(s)")
    public void iShouldReceiveUnavailabilityPeriods(int expectedCount) {
        assertNotNull(retrievedAvailabilities);
        assertEquals(expectedCount, retrievedAvailabilities.size());
    }

    @Then("the periods should include dates from {string} to {string}")
    public void thePeriodsShouldIncludeDatesFromTo(String startDate, String endDate) {
        assertNotNull(retrievedAvailabilities);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        boolean found = retrievedAvailabilities.stream()
            .anyMatch(a -> a.getStartDate().equals(start) && a.getEndDate().equals(end));

        assertTrue(found, "Expected to find period from " + startDate + " to " + endDate);
    }

    @Then("the unavailability period should be deleted successfully")
    public void theUnavailabilityPeriodShouldBeDeletedSuccessfully() {
        assertNotNull(lastAvailabilityResponse);
        assertEquals(HttpStatus.OK, lastAvailabilityResponse.getStatusCode());
    }
}
