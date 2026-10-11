package com.ampt.ui;

import com.ampt.ui.pages.AlertsPage;
import com.ampt.ui.pages.DashboardPage;
import com.ampt.ui.pages.LoginPage;
import com.ampt.ui.pages.PriceFormPage;
import com.ampt.ui.pages.PriceSearchPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AmptJourneysIT extends BaseUiTest {

    @Test
    @Order(1)
    @DisplayName("Journey 1: Login as entry user")
    public void testJourney1_LoginAsEntryUser() {
        navigate("/login");
        LoginPage loginPage = new LoginPage(driver);

        loginPage.login("entry", "entry123");

        // Verify redirect to /prices and session status in navbar
        wait.until(ExpectedConditions.urlContains("/prices"));
        WebElement userBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".badge-user, .badge")));
        assertTrue(userBadge.getText().contains("entry"), "Navbar should display logged-in username 'entry'");
        assertTrue(driver.getPageSource().contains("ENTRY_USER"), "Role ENTRY_USER should be displayed");
    }

    @Test
    @Order(2)
    @DisplayName("Journey 2: Add a price record with validation")
    public void testJourney2_AddPriceRecord() {
        // Authenticate first
        navigate("/login");
        new LoginPage(driver).login("entry", "entry123");
        wait.until(ExpectedConditions.urlContains("/prices"));

        navigate("/prices/new");
        PriceFormPage formPage = new PriceFormPage(driver);

        // Fill form with valid figures: Market 1, Commodity 1, Date, Qty, Min, Max, Modal
        formPage.createPriceRecord(1, 1, "2026-10-18", "320.00", "2000.00", "2600.00", "2300.00");

        // Verify successful redirect to directory with confirmation
        wait.until(ExpectedConditions.urlContains("/prices"));
        PriceSearchPage searchPage = new PriceSearchPage(driver);
        assertTrue(searchPage.isSuccessAlertPresent(), "Success banner should be visible after creating record");
        assertTrue(searchPage.getSuccessAlertText().toLowerCase().contains("successfully"),
                "Success banner text should confirm creation");
    }

    @Test
    @Order(3)
    @DisplayName("Journey 3: Search and filter prices by commodity and date")
    public void testJourney3_SearchAndFilterPrices() {
        navigate("/prices");
        PriceSearchPage searchPage = new PriceSearchPage(driver);

        // Filter by commodity index 1 (Tomato), and date window
        searchPage.selectCommodityByIndex(1);
        searchPage.setDateRange("2026-10-01", "2026-10-20");
        searchPage.clickFilter();

        // Wait for filtered records
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        int rowCount = searchPage.getTableRowCount();
        assertTrue(rowCount > 0, "Filtered search should return at least 1 record matching criteria");
        assertTrue(driver.getCurrentUrl().contains("commodityId="), "URL should include commodityId query parameter");
    }

    @Test
    @Order(4)
    @DisplayName("Journey 4: Dashboard summary indicators and drill-down navigation")
    public void testJourney4_DashboardIndicatorsAndDrillDown() {
        navigate("/");
        DashboardPage dashboardPage = new DashboardPage(driver);

        assertTrue(dashboardPage.isLoaded(), "Dashboard should load summary indicators section");
        assertTrue(dashboardPage.getStatCardsCount() >= 3, "Dashboard should display key KPI stat cards");

        assertFalse(dashboardPage.getDrillDownLinks().isEmpty(), "Dashboard should display drill-down links for commodities");
        dashboardPage.clickFirstDrillDownLink();

        // Verify drill-down navigates to filtered price list
        wait.until(ExpectedConditions.urlContains("/prices"));
        assertTrue(driver.getCurrentUrl().contains("commodityId="), "Drill-down link must filter by commodity");
    }

    @Test
    @Order(5)
    @DisplayName("Journey 5: Surveillance alerts page reflects price spike")
    public void testJourney5_AlertsPageShowsSpikeAlert() {
        // Authenticate to add record
        navigate("/login");
        new LoginPage(driver).login("entry", "entry123");
        wait.until(ExpectedConditions.urlContains("/prices"));

        // Enter a severe price spike (modal = 6500 when baseline was ~2150, >> 15% threshold)
        navigate("/prices/new");
        PriceFormPage formPage = new PriceFormPage(driver);
        formPage.createPriceRecord(1, 1, "2026-10-28", "120.00", "5500.00", "7500.00", "6500.00");
        wait.until(ExpectedConditions.urlContains("/prices"));

        // Navigate to /alerts and verify the active anomaly alert
        navigate("/alerts");
        AlertsPage alertsPage = new AlertsPage(driver);

        assertTrue(alertsPage.isLoaded(), "Alerts surveillance dashboard should be loaded");
        assertTrue(alertsPage.getAlertCardsCount() >= 1, "At least one exception/spike alert should be visible");
        assertTrue(alertsPage.hasAlertContainingText("THRESHOLD_BREACH") || alertsPage.hasAlertContainingText("Spike"),
                "Alerts page should display threshold breach spike");
    }
}
