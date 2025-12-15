package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.dto.MusicSheetRegistrationRequest;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
import com.example.OLSHEETS.repository.UserRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
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

public class RegisterMusicSheetSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    @Autowired
    private SheetBookingRepository sheetBookingRepository;

    @Autowired
    private UserRepository userRepository;

    private Long currentOwnerId;
    private ResponseEntity<?> lastRegistrationResponse;
    private MusicSheet lastRegisteredMusicSheet;
    private int sheetCountBeforeRegistration;
    private String lastRequestName;

    @Before("@register_music_sheet")
    public void setUp() {
        sheetBookingRepository.deleteAll();
        musicSheetRepository.deleteAll();
    }

    @Given("I am a sheet owner with ID {int}")
    public void iAmASheetOwnerWithID(Integer ownerId) {
        // Create a user for this scenario and use its actual DB id as owner id
        User saved = userRepository.save(new User("sheetowner" + ownerId, "sheetowner" + ownerId + "@a.com", "Sheet Owner " + ownerId, "password123"));
        this.currentOwnerId = saved.getId();
        this.sheetCountBeforeRegistration = (int) musicSheetRepository.count();
    }

    @When("I register a/another music sheet with the following details:")
    public void iRegisterAMusicSheetWithTheFollowingDetails(DataTable dataTable) {
        Map<String, String> details = dataTable.asMap(String.class, String.class);
        registerMusicSheet(details);
    }

    private void registerMusicSheet(Map<String, String> details) {
        MusicSheetRegistrationRequest request = new MusicSheetRegistrationRequest();
        request.setName(details.get("name"));
        lastRequestName = details.get("name");
        request.setDescription(details.get("description"));
        request.setPrice(Double.parseDouble(details.get("price")));
        request.setOwnerId(currentOwnerId);
        request.setCategory(details.get("category"));
        request.setComposer(details.get("composer"));
        request.setInstrumentation(details.get("instrumentation"));
        
        String durationStr = details.get("duration");
        if (durationStr != null && !durationStr.isEmpty()) {
            request.setDuration(Float.parseFloat(durationStr));
        }

        String photos = details.get("photos");
        if (photos != null && !photos.isEmpty()) {
            List<String> photoList = Arrays.asList(photos.split(","));
            request.setPhotoPaths(photoList);
        }

        String url = "http://localhost:" + port + "/api/sheets/register";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MusicSheetRegistrationRequest> entity = new HttpEntity<>(request, headers);

        // Use String to avoid Jackson polymorphic deserialization issues
        ResponseEntity<String> stringResponse = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String.class
        );

        lastRegistrationResponse = ResponseEntity.status(stringResponse.getStatusCode()).build();
        
        // Parse manually if successful
        if (stringResponse.getStatusCode() == HttpStatus.CREATED && stringResponse.getBody() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                lastRegisteredMusicSheet = mapper.readValue(stringResponse.getBody(), MusicSheet.class);
            } catch (Exception e) {
                // If parsing fails, we'll get from repository
                List<MusicSheet> sheets = musicSheetRepository.findByNameContainingIgnoreCase(lastRequestName);
                if (!sheets.isEmpty()) {
                    lastRegisteredMusicSheet = sheets.get(0);
                }
            }
        }
    }

    @Then("the music sheet should be successfully registered")
    public void theMusicSheetShouldBeSuccessfullyRegistered() {
        assertNotNull(lastRegistrationResponse);
        assertEquals(HttpStatus.CREATED, lastRegistrationResponse.getStatusCode());
        // Ensure we have the music sheet from the repository if not parsed from response
        if (lastRegisteredMusicSheet == null) {
            List<MusicSheet> sheets = musicSheetRepository.findByNameContainingIgnoreCase(lastRequestName);
            if (!sheets.isEmpty()) {
                lastRegisteredMusicSheet = sheets.get(0);
            }
        }
        assertNotNull(lastRegisteredMusicSheet);
        assertNotNull(lastRegisteredMusicSheet.getId());
    }

    @Then("the music sheet should have {int} photo/photos attached")
    public void theMusicSheetShouldHavePhotosAttached(int expectedPhotoCount) {
        assertNotNull(lastRegisteredMusicSheet);
        if (expectedPhotoCount == 0) {
            assertTrue(lastRegisteredMusicSheet.getFileReferences() == null || 
                      lastRegisteredMusicSheet.getFileReferences().isEmpty());
        } else {
            assertNotNull(lastRegisteredMusicSheet.getFileReferences());
            assertEquals(expectedPhotoCount, lastRegisteredMusicSheet.getFileReferences().size());
        }
    }

    @Then("I should be able to search for the sheet by name {string}")
    public void iShouldBeAbleToSearchForMusicSheetByName(String searchName) {
        String url = "http://localhost:" + port + "/api/sheets/search?name=" + searchName;

        ResponseEntity<List<MusicSheet>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<MusicSheet>>() {}
        );

        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        boolean found = response.getBody().stream()
            .anyMatch(s -> s.getName().equals(searchName));
        assertTrue(found, "Music sheet with name '" + searchName + "' should be found in search results");
    }

    @Then("the registered music sheet should have name {string}")
    public void theRegisteredMusicSheetShouldHaveName(String expectedName) {
        assertNotNull(lastRegisteredMusicSheet);
        assertEquals(expectedName, lastRegisteredMusicSheet.getName());
    }

    @Then("the registered music sheet should have description {string}")
    public void theRegisteredMusicSheetShouldHaveDescription(String expectedDescription) {
        assertNotNull(lastRegisteredMusicSheet);
        assertEquals(expectedDescription, lastRegisteredMusicSheet.getDescription());
    }

    @Then("the registered music sheet should have price {double}")
    public void theRegisteredMusicSheetShouldHavePrice(Double expectedPrice) {
        assertNotNull(lastRegisteredMusicSheet);
        assertEquals(expectedPrice, lastRegisteredMusicSheet.getPrice());
    }

    @Then("both music sheets should be successfully registered")
    public void bothMusicSheetsShouldBeSuccessfullyRegistered() {
        long currentCount = musicSheetRepository.count();
        assertEquals(sheetCountBeforeRegistration + 2, currentCount);
    }

    @Then("I should be able to search for {string} and find {int} music sheet")
    public void iShouldBeAbleToSearchForAndFindMusicSheet(String searchName, int expectedCount) {
        String url = "http://localhost:" + port + "/api/sheets/search?name=" + searchName;

        ResponseEntity<List<MusicSheet>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<MusicSheet>>() {}
        );

        assertNotNull(response.getBody());
        assertEquals(expectedCount, response.getBody().size());
    }

    @Then("the registered music sheet should have all the details I provided")
    public void theRegisteredMusicSheetShouldHaveAllTheDetailsIProvided() {
        assertNotNull(lastRegisteredMusicSheet);
        assertNotNull(lastRegisteredMusicSheet.getId());
        assertNotNull(lastRegisteredMusicSheet.getName());
        assertNotNull(lastRegisteredMusicSheet.getDescription());
        assertNotNull(lastRegisteredMusicSheet.getPrice());
        assertEquals(currentOwnerId, lastRegisteredMusicSheet.getOwnerId());
        assertNotNull(lastRegisteredMusicSheet.getCategory());
        assertNotNull(lastRegisteredMusicSheet.getComposer());
    }

    @Then("I should be able to filter by category {string} and find the sheet {string}")
    public void iShouldBeAbleToFilterByCategoryAndFindTheSheet(String category, String sheetName) {
        String url = "http://localhost:" + port + "/api/sheets/filter/category?category=" + category;

        ResponseEntity<List<MusicSheet>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<MusicSheet>>() {}
        );

        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        boolean found = response.getBody().stream()
            .anyMatch(s -> s.getName().equals(sheetName));
        assertTrue(found, "Music sheet '" + sheetName + "' should be found when filtering by category '" + category + "'");
    }
}
