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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterSheetsByCategorySteps {

    @LocalServerPort
    private int port;

    @Autowired
    private MusicSheetRepository musicSheetRepository;

    @Autowired
    private SheetBookingRepository sheetBookingRepository;

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

    @Given("the following music sheets exist for category filter:")
    public void theFollowingMusicSheetsExistForCategoryFilter(DataTable dataTable) {
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
            com.example.OLSHEETS.data.User owner = new com.example.OLSHEETS.data.User("owner1");
            owner.setId(1L);
            sheet.setOwner(owner); // Default owner for test data

            musicSheetRepository.save(sheet);
        }
    }

    @When("I filter music sheets by category {string}")
    public void iFilterMusicSheetsByCategory(String category) {
        driver.get(FRONTEND_URL);

        // Switch to sheets tab
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Music Sheets')]")))
                .click();

        // Wait for filter dropdown
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("filter-category-select")));

        // Select category from dropdown
        Select categorySelect = new Select(driver.findElement(By.id("filter-category-select")));
        categorySelect.selectByValue(category);

        // Click filter button
        driver.findElement(By.id("filter-category-btn")).click();

        // Wait for results - increased timeout
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("the filter should return {int} music sheet(s)")
    public void theFilterShouldReturnMusicSheets(int count) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sheets-grid")));
        List<WebElement> results = driver.findElements(By.cssSelector(".sheet-result"));
        assertEquals(count, results.size());
    }

    @Then("the first filtered music sheet should have name {string}")
    public void theFirstFilteredMusicSheetShouldHaveName(String expectedName) {
        List<WebElement> results = driver.findElements(By.cssSelector(".sheet-result"));
        assertTrue(results.size() > 0, "No music sheets found");

        WebElement firstResult = results.get(0);
        String actualName = firstResult.findElement(By.tagName("h3")).getText();
        assertEquals(expectedName, actualName);
    }

    @Then("all filtered music sheets should have category {string}")
    public void allFilteredMusicSheetsShouldHaveCategory(String expectedCategory) {
        List<WebElement> results = driver.findElements(By.cssSelector(".sheet-result"));

        for (WebElement result : results) {
            String resultText = result.getText();
            assertTrue(resultText.contains(expectedCategory),
                    "Expected result to contain category " + expectedCategory + " but got: " + resultText);
        }
    }
}
