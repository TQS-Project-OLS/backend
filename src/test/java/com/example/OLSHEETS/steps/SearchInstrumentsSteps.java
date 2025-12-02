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

public class SearchInstrumentsSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private com.example.OLSHEETS.repository.BookingRepository bookingRepository;

    private List<Instrument> searchResults;

    @Given("the following instruments exist:")
    public void theFollowingInstrumentsExist(DataTable dataTable) {
        bookingRepository.deleteAll();
        instrumentRepository.deleteAll();

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            Instrument instrument = new Instrument();
            instrument.setName(row.get("name"));

            // Parse type string to InstrumentType enum
            String typeStr = row.get("type");
            if (typeStr != null && !typeStr.isEmpty()) {
                try {
                    instrument.setType(InstrumentType.valueOf(typeStr.toUpperCase().replace(" ", "_")));
                } catch (IllegalArgumentException e) {
                    instrument.setType(mapLegacyTypeToEnum(typeStr));
                }
            }

            instrument.setFamily(InstrumentFamily.valueOf(row.get("family").toUpperCase()));
            instrument.setAge(Integer.parseInt(row.get("age")));
            instrument.setPrice(Double.parseDouble(row.get("price")));
            instrument.setDescription(row.get("description"));
            instrument.setOwnerId(1); // Default owner for test data

            instrumentRepository.save(instrument);
        }
    }

    private InstrumentType mapLegacyTypeToEnum(String legacyType) {
        // Map legacy string values to enum values
        return switch (legacyType.toLowerCase()) {
            case "digital piano", "digital" -> InstrumentType.DIGITAL;
            case "electric guitar", "electric" -> InstrumentType.ELECTRIC;
            case "acoustic guitar", "acoustic" -> InstrumentType.ACOUSTIC;
            case "alto sax", "saxophone" -> InstrumentType.WIND;
            case "bass guitar", "bass" -> InstrumentType.BASS;
            case "drums" -> InstrumentType.DRUMS;
            case "synthesizer", "synth" -> InstrumentType.SYNTHESIZER;
            default -> InstrumentType.ACOUSTIC; // Default fallback
        };
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
