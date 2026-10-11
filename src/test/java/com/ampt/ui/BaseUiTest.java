package com.ampt.ui;

import com.ampt.ui.extension.ScreenshotOnFailureExtension;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseUiTest implements ScreenshotOnFailureExtension.DriverProvider {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;

    @BeforeAll
    public static void setupWebDriverManager() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupDriver() {
        baseUrl = System.getProperty("base.url", "http://localhost:8082");
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        // Comprehensive Chrome binary detection across Windows user profiles & Program Files
        String[] possibleChromePaths = {
            "C:\\Users\\91976\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe",
            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
            "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
            System.getenv("LOCALAPPDATA") != null ? System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe" : null,
            System.getenv("PROGRAMFILES") != null ? System.getenv("PROGRAMFILES") + "\\Google\\Chrome\\Application\\chrome.exe" : null,
            System.getenv("PROGRAMFILES(X86)") != null ? System.getenv("PROGRAMFILES(X86)") + "\\Google\\Chrome\\Application\\chrome.exe" : null
        };

        for (String path : possibleChromePaths) {
            if (path != null) {
                File candidate = new File(path);
                if (candidate.exists()) {
                    options.setBinary(candidate);
                    break;
                }
            }
        }

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Override
    public WebDriver getDriver() {
        return driver;
    }

    protected void navigate(String path) {
        String url = path.startsWith("http") ? path : baseUrl + (path.startsWith("/") ? path : "/" + path);
        driver.get(url);
    }
}
