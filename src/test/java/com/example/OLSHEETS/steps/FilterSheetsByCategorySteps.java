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

public class FilterSheetsByCategorySteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    private List<MusicSheet> filterResults;

    @Given("the following music sheets exist for category filter:")
    public void theFollowingMusicSheetsExistForCategoryFilter(DataTable dataTable) {
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

    @When("I filter music sheets by category {string}")
    public void iFilterMusicSheetsByCategory(String category) {
        String url = "http://localhost:" + port + "/api/sheets/filter/category?category=" + category;

        ResponseEntity<List<MusicSheet>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<MusicSheet>>() {}
        );

        filterResults = response.getBody();
    }

    @Then("the filter should return {int} music sheet(s)")
    public void theFilterShouldReturnMusicSheets(int count) {
        assertNotNull(filterResults);
        assertEquals(count, filterResults.size());
    }

    @Then("the first filtered music sheet should have name {string}")
    public void theFirstFilteredMusicSheetShouldHaveName(String expectedName) {
        assertNotNull(filterResults);
        assertEquals(expectedName, filterResults.get(0).getName());
    }

    @Then("all filtered music sheets should have category {string}")
    public void allFilteredMusicSheetsShouldHaveCategory(String expectedCategory) {
        assertNotNull(filterResults);
        for (MusicSheet sheet : filterResults) {
            assertEquals(expectedCategory, sheet.getCategory());
        }
    }
}
