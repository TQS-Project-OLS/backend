package com.example.OLSHEETS.steps;

import com.example.OLSHEETS.data.MusicSheet;
import com.example.OLSHEETS.repository.MusicSheetRepository;
import com.example.OLSHEETS.repository.SheetBookingRepository;
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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchMusicSheetsSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    @Autowired
    private SheetBookingRepository sheetBookingRepository;

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String FRONTEND_URL = "http://localhost:5000";

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

    @Given("the following music sheets exist:")
    public void theFollowingMusicSheetsExist(DataTable dataTable) {
        sheetBookingRepository.deleteAll();
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
        driver.get(FRONTEND_URL);

        // Switch to sheets tab
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Music Sheets')]")))
                .click();

        // Enter search term
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sheet-search-input")));
        searchInput.clear();
        searchInput.sendKeys(name);

        // Click search button
        driver.findElement(By.id("search-sheets-btn")).click();

        // Wait for results to load - increased timeout
        try {
            Thread.sleep(3000); // Give more time for async call and rendering
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("I should receive {int} music sheet(s)")
    public void iShouldReceiveMusicSheets(int count) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sheets-grid")));

        // Wait a bit more for results to render
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<WebElement> results = driver.findElements(By.cssSelector(".sheet-result"));
        assertEquals(count, results.size(), "Expected " + count + " music sheet(s) but found " + results.size());
    }

    @Then("the first music sheet should have name {string}")
    public void theFirstMusicSheetShouldHaveName(String expectedName) {
        List<WebElement> results = driver.findElements(By.cssSelector(".sheet-result"));
        assertTrue(results.size() > 0, "No music sheets found");

        WebElement firstResult = results.get(0);
        String actualName = firstResult.findElement(By.tagName("h3")).getText();
        assertEquals(expectedName, actualName);
    }
}
