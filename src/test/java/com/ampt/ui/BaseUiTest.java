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

        // Fallback for custom Windows user AppData Chrome installations
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null) {
            File userChrome = new File(localAppData, "Google\\Chrome\\Application\\chrome.exe");
            if (userChrome.exists()) {
                options.setBinary(userChrome);
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
