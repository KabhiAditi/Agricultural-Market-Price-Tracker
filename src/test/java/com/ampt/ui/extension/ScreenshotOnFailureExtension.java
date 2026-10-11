package com.ampt.ui.extension;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    private static final String SCREENSHOT_DIR = "target/screenshots";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public interface DriverProvider {
        WebDriver getDriver();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isPresent()) {
            Object testInstance = context.getRequiredTestInstance();
            if (testInstance instanceof DriverProvider provider) {
                WebDriver driver = provider.getDriver();
                if (driver != null) {
                    captureScreenshot(driver, context);
                }
            }
        }
    }

    private void captureScreenshot(WebDriver driver, ExtensionContext context) {
        try {
            if (driver instanceof TakesScreenshot takesScreenshot) {
                File scrFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
                Path outputDir = Paths.get(SCREENSHOT_DIR);
                if (!Files.exists(outputDir)) {
                    Files.createDirectories(outputDir);
                }

                String className = context.getRequiredTestClass().getSimpleName();
                String methodName = context.getRequiredTestMethod().getName();
                String timestamp = LocalDateTime.now().format(FORMATTER);
                String filename = String.format("%s_%s_%s.png", className, methodName, timestamp);

                Path destination = outputDir.resolve(filename);
                Files.copy(scrFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[SELENIUM] Test failed! Captured screenshot to: " + destination.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("[SELENIUM] Failed to capture failure screenshot: " + e.getMessage());
        }
    }
}
