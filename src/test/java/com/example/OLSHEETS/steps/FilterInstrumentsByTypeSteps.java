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
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterInstrumentsByTypeSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private com.example.OLSHEETS.repository.BookingRepository bookingRepository;

    @Autowired
    private com.example.OLSHEETS.repository.UserRepository userRepository;

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String FRONTEND_URL = "http://localhost:8080";

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Given("the following instruments exist for type filter:")
    public void theFollowingInstrumentsExistForTypeFilter(DataTable dataTable) {
        bookingRepository.deleteAll();
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
            com.example.OLSHEETS.data.User owner = new com.example.OLSHEETS.data.User("owner1");
            owner = userRepository.save(owner);
            instrument.setOwner(owner); // Default owner for test data

            instrumentRepository.save(instrument);
        }
    }

    @When("I filter instruments by type {string}")
    public void iFilterInstrumentsByType(String type) {
        driver.get(FRONTEND_URL);

        // Wait for page to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("filter-type-select")));

        // Select type from dropdown
        Select typeSelect = new Select(driver.findElement(By.id("filter-type-select")));
        typeSelect.selectByValue(type);

        // Click filter button
        driver.findElement(By.id("filter-type-btn")).click();

        // Wait for results - increased timeout
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("the filter should return {int} instrument(s)")
    public void theFilterShouldReturnInstruments(int count) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("instruments-grid")));
        List<WebElement> results = driver.findElements(By.cssSelector(".instrument-result"));
        assertEquals(count, results.size());
    }

    @Then("the first filtered instrument should have name {string}")
    public void theFirstFilteredInstrumentShouldHaveName(String expectedName) {
        List<WebElement> results = driver.findElements(By.cssSelector(".instrument-result"));
        assertTrue(results.size() > 0, "No instruments found");

        WebElement firstResult = results.get(0);
        String actualName = firstResult.findElement(By.tagName("h3")).getText();
        assertEquals(expectedName, actualName);
    }

    @Then("all filtered instruments should have type {string}")
    public void allFilteredInstrumentsShouldHaveType(String expectedType) {
        List<WebElement> results = driver.findElements(By.cssSelector(".instrument-result"));

        for (WebElement result : results) {
            String resultText = result.getText();
            assertTrue(resultText.contains(expectedType),
                    "Expected result to contain type " + expectedType + " but got: " + resultText);
        }
    }
}
