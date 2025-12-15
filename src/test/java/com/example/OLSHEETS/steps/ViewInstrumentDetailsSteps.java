package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.*;
import com.example.OLSHEETS.repository.InstrumentRepository;
import com.example.OLSHEETS.repository.UserRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ViewInstrumentDetailsSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private UserRepository userRepository;

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String FRONTEND_URL = "http://localhost:8080";
    private static final String API_BASE_URL = "http://localhost:8080/api";

    private Instrument currentInstrument;
    private String currentInstrumentName;
    private String errorMessage;

    @Before
    public void setUp() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Given("the following instrument exists in the catalog:")
    public void theFollowingInstrumentExistsInTheCatalog(DataTable dataTable) {
        // Clean database
        instrumentRepository.deleteAll();
        userRepository.deleteAll();

        Map<String, String> data = dataTable.asMap(String.class, String.class);
        createInstrumentFromData(data);
    }

    @Given("the following instruments exist in the catalog:")
    public void theFollowingInstrumentsExistInTheCatalog(DataTable dataTable) {
        // Clean database
        instrumentRepository.deleteAll();
        userRepository.deleteAll();

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            createInstrumentFromData(row);
        }
    }

    @Given("no instruments exist in the catalog")
    public void noInstrumentsExistInTheCatalog() {
        instrumentRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void createInstrumentFromData(Map<String, String> data) {
        Instrument instrument = new Instrument();
        instrument.setName(data.get("name"));
        instrument.setDescription(data.get("description"));
        instrument.setPrice(Double.parseDouble(data.get("price")));
        instrument.setAge(Integer.parseInt(data.get("age")));
        instrument.setType(InstrumentType.valueOf(data.get("type")));
        instrument.setFamily(InstrumentFamily.valueOf(data.get("family")));

        // Create and save owner
        User owner = new User("testowner", "testowner@example.com", "password123");
        owner = userRepository.save(owner);
        instrument.setOwner(owner);

        // Add photos if present
        String photos = data.get("photos");
        if (photos != null && !photos.isEmpty()) {
            List<FileReference> fileReferences = new ArrayList<>();
            String[] photoPaths = photos.split(",");
            for (String photoPath : photoPaths) {
                FileReference fileRef = new FileReference("photo", photoPath.trim(), instrument);
                fileReferences.add(fileRef);
            }
            instrument.setFileReferences(fileReferences);
        }

        instrumentRepository.save(instrument);
    }

    @When("I view the details of instrument {string}")
    public void iViewTheDetailsOfInstrument(String instrumentName) {
        currentInstrumentName = instrumentName;
        
        // Find the instrument by name
        List<Instrument> instruments = instrumentRepository.findByNameContainingIgnoreCase(instrumentName);
        assertFalse(instruments.isEmpty(), "Instrument not found: " + instrumentName);
        currentInstrument = instruments.get(0);

        // Navigate to API endpoint to get instrument details
        driver.get(API_BASE_URL + "/instruments/" + currentInstrument.getId());

        // Wait for page to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    @When("I attempt to view the details of instrument with ID {int}")
    public void iAttemptToViewTheDetailsOfInstrumentWithID(int id) {
        try {
            // Navigate to API endpoint
            driver.get(API_BASE_URL + "/instruments/" + id);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Check if there's an error in the response
            String bodyText = driver.findElement(By.tagName("body")).getText();
            if (bodyText.contains("Instrument not found")) {
                errorMessage = "Instrument not found";
            }
        } catch (Exception e) {
            errorMessage = "Instrument not found";
        }
    }

    @Then("I should see the instrument name {string}")
    public void iShouldSeeTheInstrumentName(String expectedName) {
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("\"name\":\"" + expectedName + "\""), 
            "Expected to see instrument name: " + expectedName);
    }

    @Then("I should see the description {string}")
    public void iShouldSeeTheDescription(String expectedDescription) {
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("\"description\":\"" + expectedDescription + "\""), 
            "Expected to see description: " + expectedDescription);
    }

    @Then("I should see the description containing {string}")
    public void iShouldSeeTheDescriptionContaining(String expectedSubstring) {
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains(expectedSubstring), 
            "Expected description to contain: " + expectedSubstring);
    }

    @Then("I should see the price {string}")
    public void iShouldSeeThePrice(String expectedPrice) {
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("\"price\":" + expectedPrice), 
            "Expected to see price: " + expectedPrice);
    }

    @Then("I should see the age {string}")
    public void iShouldSeeTheAge(String expectedAge) {
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("\"age\":" + expectedAge), 
            "Expected to see age: " + expectedAge);
    }

    @Then("I should see the type {string}")
    public void iShouldSeeTheType(String expectedType) {
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("\"type\":\"" + expectedType + "\""), 
            "Expected to see type: " + expectedType);
    }

    @Then("I should see the family {string}")
    public void iShouldSeeTheFamily(String expectedFamily) {
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("\"family\":\"" + expectedFamily + "\""), 
            "Expected to see family: " + expectedFamily);
    }

    @Then("I should see {int} photo(s)")
    public void iShouldSeePhotos(int expectedPhotoCount) {
        // Verify the instrument has the expected number of photos
        assertNotNull(currentInstrument, "Current instrument should not be null");
        
        // Reload from database to get file references
        Instrument reloaded = instrumentRepository.findById(currentInstrument.getId()).orElse(null);
        assertNotNull(reloaded, "Instrument should exist in database");
        
        List<FileReference> fileReferences = reloaded.getFileReferences();
        int actualPhotoCount = (fileReferences == null) ? 0 : fileReferences.size();
        
        assertEquals(expectedPhotoCount, actualPhotoCount, 
            "Expected " + expectedPhotoCount + " photo(s) but found " + actualPhotoCount);
    }

    @Then("I should receive an error message {string}")
    public void iShouldReceiveAnErrorMessage(String expectedError) {
        assertNotNull(errorMessage, "Expected an error message but none was received");
        assertTrue(errorMessage.contains(expectedError) || errorMessage.equals(expectedError), 
            "Expected error message: " + expectedError + " but got: " + errorMessage);
    }
}
