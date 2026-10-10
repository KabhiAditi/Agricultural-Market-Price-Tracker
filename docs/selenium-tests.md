# Automated UI Testing Strategy (Selenium WebDriver)

## 1. Overview & Purpose
In accordance with the DevOps quality gates established for the **Agricultural Market Price Tracker (AMPT)**, automated end-to-end (E2E) UI testing is planned for **Task 12**. Automated Selenium tests ensure that critical browser-based workflows function correctly across releases and serve as a mandatory CI/CD deployment gate within Jenkins.

---

## 2. Testing Framework & Tooling
- **Test Runner**: JUnit 5 (`org.junit.jupiter`)
- **Automation Framework**: Selenium WebDriver 4.x (`selenium-java`)
- **Driver Management**: `io.github.bonigarcia:webdrivermanager` (automatic browser driver resolution)
- **Target Browser**: Headless Google Chrome & Mozilla Firefox
- **Reporting**: Surefire / Allure Test Reports integrated into Jenkins

---

## 3. Critical User Journeys (Test Scope)

| Test ID | User Journey | Target Component | Backlog Mapping | Gating Policy |
|---------|--------------|------------------|-----------------|---------------|
| `SEL-01` | **Dashboard Loading & Indicators** | Verify summary indicators (average price, % change, markets reporting) render with valid values on `/` | B08, B10 | Pipeline Blocking |
| `SEL-02` | **Price Search & Filtering** | Enter commodity/market filter, click search, verify result table updates with matched records | B06, B07 | Pipeline Blocking |
| `SEL-03` | **Price Entry Form Validation** | Submit empty price form, verify client-side and server-side validation error messages appear | B03 | Pipeline Blocking |
| `SEL-04` | **Authorized Record Creation** | Log in as entry clerk, submit valid price record, verify record appears in search list | B03, B05 | Pipeline Blocking |
| `SEL-05` | **Alerts & Exceptions View** | Navigate to `/alerts`, verify threshold breach and missing data notifications are displayed | B13, B14, B15 | Non-blocking / Warning |

---

## 4. CI/CD Integration & Headless Execution
In the headless Jenkins runner and Docker environments, tests are executed using:
```bash
# Run Selenium E2E Suite in headless mode
mvn test -Dtest=*SeleniumTest -Dheadless=true
```

### Chrome Headless Configuration (Java)
```java
ChromeOptions options = new ChromeOptions();
options.addArguments("--headless=new");
options.addArguments("--no-sandbox");
options.addArguments("--disable-dev-shm-usage");
options.addArguments("--window-size=1920,1080");
WebDriver driver = new ChromeDriver(options);
```

---

## 5. Artifacts and Evidence Retention
- On test failure, automated screenshots are captured and saved to `docs/evidence/selenium/` or Jenkins build artifacts.
- Jenkins pipeline archives JUnit test results (`target/surefire-reports/*.xml`).
