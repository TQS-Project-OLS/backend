package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.*;
import com.example.OLSHEETS.repository.MusicSheetRepository;
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

public class ViewMusicSheetDetailsSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    @Autowired
    private UserRepository userRepository;

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080";

    private MusicSheet currentMusicSheet;
    private String currentMusicSheetName;
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

    @Given("the following music sheet exists in the catalog:")
    public void theFollowingMusicSheetExistsInTheCatalog(DataTable dataTable) {
        // Clean database
        musicSheetRepository.deleteAll();
        userRepository.deleteAll();

        Map<String, String> data = dataTable.asMap(String.class, String.class);
        createMusicSheetFromData(data);
    }

    @Given("the following music sheets exist in the catalog:")
    public void theFollowingMusicSheetsExistInTheCatalog(DataTable dataTable) {
        // Clean database
        musicSheetRepository.deleteAll();
        userRepository.deleteAll();

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            createMusicSheetFromData(row);
        }
    }

    @Given("no music sheets exist in the catalog")
    public void noMusicSheetsExistInTheCatalog() {
        musicSheetRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void createMusicSheetFromData(Map<String, String> data) {
        MusicSheet musicSheet = new MusicSheet();
        musicSheet.setName(data.get("name"));
        musicSheet.setDescription(data.get("description"));
        musicSheet.setPrice(Double.parseDouble(data.get("price")));
        musicSheet.setCategory(data.get("category"));
        musicSheet.setComposer(data.get("composer"));
        musicSheet.setInstrumentation(data.get("instrumentation"));
        
        String durationStr = data.get("duration");
        if (durationStr != null && !durationStr.isEmpty()) {
            musicSheet.setDuration(Float.parseFloat(durationStr));
        }

        // Create and save owner
        User owner = new User("testowner", "testowner@example.com", "password123");
        owner = userRepository.save(owner);
        musicSheet.setOwner(owner);

        // Add photos if present
        String photos = data.get("photos");
        if (photos != null && !photos.isEmpty()) {
            List<FileReference> fileReferences = new ArrayList<>();
            String[] photoPaths = photos.split(",");
            for (String photoPath : photoPaths) {
                FileReference fileRef = new FileReference("photo", photoPath.trim(), musicSheet);
                fileReferences.add(fileRef);
            }
            musicSheet.setFileReferences(fileReferences);
        }

        musicSheetRepository.save(musicSheet);
    }

    @When("I view the details of music sheet {string}")
    public void iViewTheDetailsOfMusicSheet(String sheetName) {
        currentMusicSheetName = sheetName;
        
        // Find the music sheet by name
        List<MusicSheet> sheets = musicSheetRepository.findByNameContainingIgnoreCase(sheetName);
        assertFalse(sheets.isEmpty(), "Music sheet not found: " + sheetName);
        currentMusicSheet = sheets.get(0);

        // Navigate to booking page to see sheet details
        driver.get(BASE_URL + "/sheet_booking.html?id=" + currentMusicSheet.getId());

        // Wait for page to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    @When("I attempt to view the details of music sheet with ID {int}")
    public void iAttemptToViewTheDetailsOfMusicSheetWithID(int id) {
        try {
            // Navigate to booking page
            driver.get(BASE_URL + "/sheet_booking.html?id=" + id);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Wait a bit for fetch to complete
            Thread.sleep(1500);
            
            // Check if there's an error message in the page
            String bodyText = driver.findElement(By.tagName("body")).getText();
            if (bodyText.contains("not found") || bodyText.contains("Error")) {
                errorMessage = "Music sheet not found";
            }
        } catch (Exception e) {
            errorMessage = "Music sheet not found";
        }
    }

    @Then("I should see the sheet name {string}")
    public void iShouldSeeTheSheetName(String expectedName) {
        wait.until(driver -> driver.findElement(By.tagName("body")).getText().contains(expectedName));
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains(expectedName), 
            "Expected to see music sheet name: " + expectedName);
    }

    @Then("I should see the sheet description {string}")
    public void iShouldSeeTheSheetDescription(String expectedDescription) {
        wait.until(driver -> driver.findElement(By.tagName("body")).getText().contains(expectedDescription));
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains(expectedDescription), 
            "Expected to see description: " + expectedDescription);
    }

    @Then("I should see the sheet description containing {string}")
    public void iShouldSeeTheSheetDescriptionContaining(String expectedSubstring) {
        wait.until(driver -> driver.findElement(By.tagName("body")).getText().contains(expectedSubstring));
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains(expectedSubstring), 
            "Expected description to contain: " + expectedSubstring);
    }

    @Then("I should see the sheet price {string}")
    public void iShouldSeeTheSheetPrice(String expectedPrice) {
        wait.until(driver -> driver.findElement(By.tagName("body")).getText().contains(expectedPrice));
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains(expectedPrice), 
            "Expected to see price: " + expectedPrice);
    }

    @Then("I should see the category {string}")
    public void iShouldSeeTheCategory(String expectedCategory) {
        wait.until(driver -> driver.findElement(By.tagName("body")).getText().contains(expectedCategory));
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains(expectedCategory), 
            "Expected to see category: " + expectedCategory);
    }

    @Then("I should see the composer {string}")
    public void iShouldSeeTheComposer(String expectedComposer) {
        wait.until(driver -> driver.findElement(By.tagName("body")).getText().contains(expectedComposer));
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains(expectedComposer), 
            "Expected to see composer: " + expectedComposer);
    }

    @Then("I should see the instrumentation {string}")
    public void iShouldSeeTheInstrumentation(String expectedInstrumentation) {
        wait.until(driver -> driver.findElement(By.tagName("body")).getText().contains(expectedInstrumentation));
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains(expectedInstrumentation), 
            "Expected to see instrumentation: " + expectedInstrumentation);
    }

    @Then("I should see the duration {string}")
    public void iShouldSeeTheDuration(String expectedDuration) {
        wait.until(driver -> driver.findElement(By.tagName("body")).getText().contains(expectedDuration));
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains(expectedDuration), 
            "Expected to see duration: " + expectedDuration);
    }

    @Then("I should see {int} sheet photo(s)")
    public void iShouldSeeSheetPhotos(int expectedPhotoCount) {
        // Verify the music sheet has the expected number of photos
        assertNotNull(currentMusicSheet, "Current music sheet should not be null");
        
        // Reload from database to get file references
        MusicSheet reloaded = musicSheetRepository.findById(currentMusicSheet.getId()).orElse(null);
        assertNotNull(reloaded, "Music sheet should exist in database");
        
        List<FileReference> fileReferences = reloaded.getFileReferences();
        int actualPhotoCount = (fileReferences == null) ? 0 : fileReferences.size();
        
        assertEquals(expectedPhotoCount, actualPhotoCount, 
            "Expected " + expectedPhotoCount + " photo(s) but found " + actualPhotoCount);
    }

    @Then("I should receive a sheet error message {string}")
    public void iShouldReceiveASheetErrorMessage(String expectedError) {
        assertNotNull(errorMessage, "Expected an error message but none was received");
        assertTrue(errorMessage.contains(expectedError) || errorMessage.equals(expectedError), 
            "Expected error message: " + expectedError + " but got: " + errorMessage);
    }
}
