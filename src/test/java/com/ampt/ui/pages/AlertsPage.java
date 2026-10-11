package com.ampt.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AlertsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By configBanner = By.cssSelector(".config-banner");
    private final By alertCards = By.cssSelector(".alert-card");
    private final By emptyState = By.cssSelector(".empty-state");

    public AlertsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(configBanner)).isDisplayed();
    }

    public int getAlertCardsCount() {
        wait.until(d -> !d.findElements(alertCards).isEmpty() || !d.findElements(emptyState).isEmpty());
        return driver.findElements(alertCards).size();
    }

    public List<WebElement> getAlertCards() {
        return driver.findElements(alertCards);
    }

    public boolean hasAlertContainingText(String text) {
        for (WebElement card : driver.findElements(alertCards)) {
            if (card.getText().contains(text)) {
                return true;
            }
        }
        return false;
    }
}
