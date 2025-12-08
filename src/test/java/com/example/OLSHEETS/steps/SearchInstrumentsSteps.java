package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.Instrument;
import com.example.OLSHEETS.data.InstrumentType;
import com.example.OLSHEETS.data.InstrumentFamily;
import com.example.OLSHEETS.repository.InstrumentRepository;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchInstrumentsSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private com.example.OLSHEETS.repository.BookingRepository bookingRepository;

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String FRONTEND_URL = "http://localhost:8080";

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
        driver.get(FRONTEND_URL);

        // Make sure we're on instruments tab (default)
        WebElement searchInput = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("instrument-search-input")));
        searchInput.clear();
        searchInput.sendKeys(name);

        // Click search button
        driver.findElement(By.id("search-instruments-btn")).click();

        // Wait for results to load - increased timeout
        try {
            Thread.sleep(3000); // Give more time for async call and rendering
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("I should receive {int} instrument(s)")
    public void iShouldReceiveInstruments(int count) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("instruments-grid")));

        // Wait a bit more for results to render
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<WebElement> results = driver.findElements(By.cssSelector(".instrument-result"));
        assertEquals(count, results.size(), "Expected " + count + " instrument(s) but found " + results.size());
    }

    @Then("the first instrument should have name {string}")
    public void theFirstInstrumentShouldHaveName(String expectedName) {
        List<WebElement> results = driver.findElements(By.cssSelector(".instrument-result"));
        assertTrue(results.size() > 0, "No instruments found");

        WebElement firstResult = results.get(0);
        String actualName = firstResult.findElement(By.tagName("h3")).getText();
        assertEquals(expectedName, actualName);
    }
}
