package com.ampt.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By statCards = By.cssSelector(".stat-card");
    private final By drillDownLinks = By.xpath("//a[contains(text(), 'Drill-down Records')]");
    private final By alertsLink = By.cssSelector("a[href*='/alerts']");
    private final By summaryHeading = By.xpath("//h2[contains(text(), \"Today's Commodity Summary\")]");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(summaryHeading)).isDisplayed();
    }

    public int getStatCardsCount() {
        return driver.findElements(statCards).size();
    }

    public List<WebElement> getDrillDownLinks() {
        return driver.findElements(drillDownLinks);
    }

    public void clickFirstDrillDownLink() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(drillDownLinks));
        link.click();
    }

    public void clickAlertsLink() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(alertsLink));
        link.click();
    }
}
