package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.SheetCategory;
import com.example.OLSHEETS.repository.MusicSheetRepository;
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

public class SearchMusicSheetsSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    private List<MusicSheet> searchResults;

    @Given("the following music sheets exist:")
    public void theFollowingMusicSheetsExist(DataTable dataTable) {
        musicSheetRepository.deleteAll();

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            MusicSheet sheet = new MusicSheet();
            sheet.setName(row.get("name"));
            sheet.setComposer(row.get("composer"));
            sheet.setCategory(row.get("category"));
            sheet.setPrice(Double.parseDouble(row.get("price")));
            sheet.setDescription(row.get("description"));
            sheet.setOwnerId(1); // Default owner for test data

            musicSheetRepository.save(sheet);
        }
    }

    @When("I search for music sheets with name {string}")
    public void iSearchForMusicSheetsWithName(String name) {
        String url = "http://localhost:" + port + "/api/sheets/search?name=" + name;

        ResponseEntity<List<MusicSheet>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<MusicSheet>>() {}
        );

        searchResults = response.getBody();
    }

    @Then("I should receive {int} music sheet(s)")
    public void iShouldReceiveMusicSheets(int count) {
        assertNotNull(searchResults);
        assertEquals(count, searchResults.size());
    }

    @Then("the first music sheet should have name {string}")
    public void theFirstMusicSheetShouldHaveName(String expectedName) {
        assertNotNull(searchResults);
        assertEquals(expectedName, searchResults.get(0).getName());
    }
}
