package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.repository.InstrumentRepository;
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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FilterInstrumentsByTypeSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InstrumentRepository instrumentRepository;

    private List<Instrument> filterResults;

    @Given("the following instruments exist for type filter:")
    public void theFollowingInstrumentsExistForTypeFilter(DataTable dataTable) {
        instrumentRepository.deleteAll();

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Instrument instrument = new Instrument();
            instrument.setName(row.get("name"));
            instrument.setType(InstrumentType.valueOf(row.get("type")));
            instrument.setFamily(InstrumentFamily.valueOf(row.get("family").toUpperCase()));
            instrument.setAge(Integer.parseInt(row.get("age")));
            instrument.setPrice(Double.parseDouble(row.get("price")));
            instrument.setDescription(row.get("description"));
            instrument.setOwnerId(1); // Default owner for test data

            instrumentRepository.save(instrument);
        }
    }

    @When("I filter instruments by type {string}")
    public void iFilterInstrumentsByType(String type) {
        String url = "http://localhost:" + port + "/api/instruments/filter/type?type=" + type;

        ResponseEntity<List<Instrument>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Instrument>>() {}
        );

        filterResults = response.getBody();
    }

    @Then("the filter should return {int} instrument(s)")
    public void theFilterShouldReturnInstruments(int count) {
        assertNotNull(filterResults);
        assertEquals(count, filterResults.size());
    }

    @Then("the first filtered instrument should have name {string}")
    public void theFirstFilteredInstrumentShouldHaveName(String expectedName) {
        assertNotNull(filterResults);
        assertEquals(expectedName, filterResults.get(0).getName());
    }

    @Then("all filtered instruments should have type {string}")
    public void allFilteredInstrumentsShouldHaveType(String expectedType) {
        assertNotNull(filterResults);
        InstrumentType expectedEnum = InstrumentType.valueOf(expectedType);
        for (Instrument instrument : filterResults) {
            assertEquals(expectedEnum, instrument.getType());
        }
    }
}
