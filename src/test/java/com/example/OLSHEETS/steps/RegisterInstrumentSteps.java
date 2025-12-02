package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.dto.InstrumentRegistrationRequest;
import com.example.OLSHEETS.repository.InstrumentRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterInstrumentSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InstrumentRepository instrumentRepository;

    private Integer currentOwnerId;
    private ResponseEntity<Instrument> lastRegistrationResponse;
    private Instrument lastRegisteredInstrument;
    private int instrumentCountBeforeRegistration;

    @Given("I am an instrument owner with ID {int}")
    public void iAmAnInstrumentOwnerWithID(Integer ownerId) {
        this.currentOwnerId = ownerId;
        this.instrumentCountBeforeRegistration = (int) instrumentRepository.count();
    }

    @When("I register an/another instrument with the following details:")
    public void iRegisterAnInstrumentWithTheFollowingDetails(DataTable dataTable) {
        Map<String, String> details = dataTable.asMap(String.class, String.class);
        registerInstrument(details);
    }

    private void registerInstrument(Map<String, String> details) {
        InstrumentRegistrationRequest request = new InstrumentRegistrationRequest();
        request.setName(details.get("name"));
        request.setDescription(details.get("description"));
        request.setPrice(Double.parseDouble(details.get("price")));
        request.setOwnerId(currentOwnerId);
        request.setAge(Integer.parseInt(details.get("age")));
        request.setType(InstrumentType.valueOf(details.get("type")));
        request.setFamily(InstrumentFamily.valueOf(details.get("family")));

        String photos = details.get("photos");
        if (photos != null && !photos.isEmpty()) {
            List<String> photoList = Arrays.asList(photos.split(","));
            request.setPhotoPaths(photoList);
        }

        String url = "http://localhost:" + port + "/api/instruments/register";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InstrumentRegistrationRequest> entity = new HttpEntity<>(request, headers);

        lastRegistrationResponse = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            Instrument.class
        );

        lastRegisteredInstrument = lastRegistrationResponse.getBody();
    }

    @Then("the instrument should be successfully registered")
    public void theInstrumentShouldBeSuccessfullyRegistered() {
        assertNotNull(lastRegistrationResponse);
        assertEquals(HttpStatus.CREATED, lastRegistrationResponse.getStatusCode());
        assertNotNull(lastRegisteredInstrument);
        assertNotNull(lastRegisteredInstrument.getId());
    }

    @Then("the instrument should have {int} photo/photos attached")
    public void theInstrumentShouldHavePhotosAttached(int expectedPhotoCount) {
        assertNotNull(lastRegisteredInstrument);
        if (expectedPhotoCount == 0) {
            assertTrue(lastRegisteredInstrument.getFileReferences() == null || 
                      lastRegisteredInstrument.getFileReferences().isEmpty());
        } else {
            assertNotNull(lastRegisteredInstrument.getFileReferences());
            assertEquals(expectedPhotoCount, lastRegisteredInstrument.getFileReferences().size());
        }
    }

    @Then("I should be able to search for it by name {string}")
    public void iShouldBeAbleToSearchForItByName(String searchName) {
        String url = "http://localhost:" + port + "/api/instruments/search?name=" + searchName;

        ResponseEntity<List<Instrument>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Instrument>>() {}
        );

        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        boolean found = response.getBody().stream()
            .anyMatch(i -> i.getName().equals(searchName));
        assertTrue(found, "Instrument with name '" + searchName + "' should be found in search results");
    }

    @Then("the registered instrument should have name {string}")
    public void theRegisteredInstrumentShouldHaveName(String expectedName) {
        assertNotNull(lastRegisteredInstrument);
        assertEquals(expectedName, lastRegisteredInstrument.getName());
    }

    @Then("the registered instrument should have description {string}")
    public void theRegisteredInstrumentShouldHaveDescription(String expectedDescription) {
        assertNotNull(lastRegisteredInstrument);
        assertEquals(expectedDescription, lastRegisteredInstrument.getDescription());
    }

    @Then("the registered instrument should have price {double}")
    public void theRegisteredInstrumentShouldHavePrice(Double expectedPrice) {
        assertNotNull(lastRegisteredInstrument);
        assertEquals(expectedPrice, lastRegisteredInstrument.getPrice());
    }

    @Then("both instruments should be successfully registered")
    public void bothInstrumentsShouldBeSuccessfullyRegistered() {
        long currentCount = instrumentRepository.count();
        assertEquals(instrumentCountBeforeRegistration + 2, currentCount);
    }

    @Then("I should be able to search for {string} and find {int} instrument")
    public void iShouldBeAbleToSearchForAndFindInstrument(String searchName, int expectedCount) {
        String url = "http://localhost:" + port + "/api/instruments/search?name=" + searchName;

        ResponseEntity<List<Instrument>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Instrument>>() {}
        );

        assertNotNull(response.getBody());
        assertEquals(expectedCount, response.getBody().size());
    }

    @Then("the registered instrument should have all the details I provided")
    public void theRegisteredInstrumentShouldHaveAllTheDetailsIProvided() {
        assertNotNull(lastRegisteredInstrument);
        assertNotNull(lastRegisteredInstrument.getId());
        assertNotNull(lastRegisteredInstrument.getName());
        assertNotNull(lastRegisteredInstrument.getDescription());
        assertNotNull(lastRegisteredInstrument.getPrice());
        assertEquals(currentOwnerId, lastRegisteredInstrument.getOwnerId());
        assertNotNull(lastRegisteredInstrument.getAge());
        assertNotNull(lastRegisteredInstrument.getType());
        assertNotNull(lastRegisteredInstrument.getFamily());
    }
}
