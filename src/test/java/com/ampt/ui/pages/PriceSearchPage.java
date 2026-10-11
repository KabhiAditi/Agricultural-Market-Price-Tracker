package com.ampt.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class PriceSearchPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By marketSelect = By.id("marketId");
    private final By commoditySelect = By.id("commodityId");
    private final By fromDateInput = By.id("from");
    private final By toDateInput = By.id("to");
    private final By filterButton = By.cssSelector("form button[type='submit']");
    private final By tableRows = By.cssSelector("table tbody tr");
    private final By successAlert = By.cssSelector(".alert-success, .alert-flash.alert-success");
    private final By addPriceButton = By.cssSelector("a[href*='/prices/new']");

    public PriceSearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void selectCommodityByIndex(int index) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(commoditySelect));
        new Select(el).selectByIndex(index);
    }

    public void selectCommodityByValue(String val) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(commoditySelect));
        new Select(el).selectByValue(val);
    }

    public void setDateRange(String fromDate, String toDate) {
        if (fromDate != null) {
            WebElement fromEl = driver.findElement(fromDateInput);
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));",
                    fromEl, fromDate);
        }
        if (toDate != null) {
            WebElement toEl = driver.findElement(toDateInput);
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));",
                    toEl, toDate);
        }
    }

    public void clickFilter() {
        wait.until(ExpectedConditions.elementToBeClickable(filterButton)).click();
    }

    public int getTableRowCount() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        return driver.findElements(tableRows).size();
    }

    public List<WebElement> getTableRows() {
        return driver.findElements(tableRows);
    }

    public boolean isSuccessAlertPresent() {
        return !driver.findElements(successAlert).isEmpty();
    }

    public String getSuccessAlertText() {
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(successAlert));
        return alert.getText();
    }

    public void clickAddPrice() {
        wait.until(ExpectedConditions.elementToBeClickable(addPriceButton)).click();
    }
}
