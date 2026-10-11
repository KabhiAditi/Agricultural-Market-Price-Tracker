package com.ampt.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By submitButton = By.cssSelector("button.btn-submit");
    private final By successAlert = By.cssSelector(".alert-success");
    private final By errorAlert = By.cssSelector(".alert-danger");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void enterUsername(String username) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        el.clear();
        el.sendKeys(username);
    }

    public void enterPassword(String password) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        el.clear();
        el.sendKeys(password);
    }

    public void clickSubmit() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickSubmit();
    }

    public boolean isSuccessAlertPresent() {
        return !driver.findElements(successAlert).isEmpty();
    }

    public boolean isErrorAlertPresent() {
        return !driver.findElements(errorAlert).isEmpty();
    }
}
