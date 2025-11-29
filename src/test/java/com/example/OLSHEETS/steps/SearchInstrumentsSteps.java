package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.entity.InstrumentEntity;
import com.example.OLSHEETS.model.Instrument;
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

public class SearchInstrumentsSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InstrumentRepository instrumentRepository;

    private List<Instrument> searchResults;

    @Given("the following instruments exist:")
    public void theFollowingInstrumentsExist(DataTable dataTable) {
        instrumentRepository.deleteAll();

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            InstrumentEntity instrument = new InstrumentEntity();
            instrument.setName(row.get("name"));
            instrument.setType(row.get("type"));
            instrument.setFamily(row.get("family"));
            instrument.setAge(Integer.parseInt(row.get("age")));
            instrument.setPrice(Double.parseDouble(row.get("price")));
            instrument.setDescription(row.get("description"));
            instrument.setOwner_id(1); // Default owner for test data

            instrumentRepository.save(instrument);
        }
    }

    @When("I search for instruments with name {string}")
    public void iSearchForInstrumentsWithName(String name) {
        String url = "http://localhost:" + port + "/api/instruments/search?name=" + name;

        ResponseEntity<List<Instrument>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Instrument>>() {}
        );

        searchResults = response.getBody();
    }

    @Then("I should receive {int} instrument(s)")
    public void iShouldReceiveInstruments(int count) {
        assertNotNull(searchResults);
        assertEquals(count, searchResults.size());
    }

    @Then("the first instrument should have name {string}")
    public void theFirstInstrumentShouldHaveName(String expectedName) {
        assertNotNull(searchResults);
        assertEquals(expectedName, searchResults.get(0).getName());
    }
}
