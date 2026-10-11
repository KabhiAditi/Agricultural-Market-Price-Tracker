package com.ampt.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PriceFormPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By marketSelect = By.id("marketId");
    private final By commoditySelect = By.id("commodityId");
    private final By recordDateInput = By.id("recordDate");
    private final By arrivalQtyInput = By.id("arrivalQty");
    private final By minPriceInput = By.id("minPrice");
    private final By maxPriceInput = By.id("maxPrice");
    private final By modalPriceInput = By.id("modalPrice");
    private final By submitButton = By.cssSelector("form button[type='submit']");
    private final By errorMessages = By.cssSelector(".error-message");

    public PriceFormPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void selectMarketByIndex(int index) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(marketSelect));
        new Select(el).selectByIndex(index);
    }

    public void selectCommodityByIndex(int index) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(commoditySelect));
        new Select(el).selectByIndex(index);
    }

    private void setInputValue(By locator, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));",
                el, value);
    }

    public void enterRecordDate(String date) {
        setInputValue(recordDateInput, date);
    }

    public void enterArrivalQty(String qty) {
        setInputValue(arrivalQtyInput, qty);
    }

    public void enterMinPrice(String price) {
        setInputValue(minPriceInput, price);
    }

    public void enterMaxPrice(String price) {
        setInputValue(maxPriceInput, price);
    }

    public void enterModalPrice(String price) {
        setInputValue(modalPriceInput, price);
    }

    public void submit() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(submitButton));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", btn);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
        if (driver.getCurrentUrl().contains("/prices/new")) {
            driver.findElement(By.tagName("form")).submit();
        }
    }

    public void createPriceRecord(int marketIndex, int commodityIndex, String date,
                                  String arrivalQty, String minPrice, String maxPrice, String modalPrice) {
        selectMarketByIndex(marketIndex);
        selectCommodityByIndex(commodityIndex);
        enterRecordDate(date);
        enterArrivalQty(arrivalQty);
        enterMinPrice(minPrice);
        enterMaxPrice(maxPrice);
        enterModalPrice(modalPrice);
        submit();
    }

    public boolean hasValidationErrors() {
        return !driver.findElements(errorMessages).isEmpty();
    }
}
