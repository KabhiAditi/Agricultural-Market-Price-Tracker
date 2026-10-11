# Task 09: Selenium End-to-End Test Plan

## 1. Objective and Scope
This document specifies the automated UI acceptance test plan for the Agricultural Market Price Tracker (AMPT) web application. The test suite uses Selenium WebDriver 4, WebDriverManager, and JUnit 5 in headless Chrome mode against a deployed or locally running AMPT instance.

## 2. Test Environment & Configuration
*   **Application Under Test (AUT):** AMPT Spring Boot Web Application
*   **Base URL:** Configurable via JVM parameter `-Dbase.url` (Default: `http://localhost:8080`, or Tomcat on `http://localhost:8081/ampt`)
*   **Browser:** Google Chrome (Headless mode `--headless=new`, `--disable-gpu`, `--no-sandbox`, `--window-size=1920,1080`)
*   **Driver Management:** WebDriverManager (Boni Garcia) + Selenium 4
*   **Design Pattern:** Page Object Model (POM) with explicit waits (`WebDriverWait` / `ExpectedConditions`)
*   **Failure Diagnostics:** Custom JUnit 5 `TestWatcher` (`ScreenshotOnFailureExtension`) automatically capturing viewport screenshots to `target/screenshots/<testName>_<timestamp>.png` on any assertion failure.

---

## 3. Test Data Specification

| Data Entity | Identifier / Field | Test Value | Description / Purpose |
| :--- | :--- | :--- | :--- |
| **User Account** | Username | `entry` | Seeded user with `ENTRY_USER` role |
| **User Account** | Password | `entry123` | Valid credentials for authentication |
| **Market Mandi** | Market Option | `Vashi APMC` (or ID `1`) | Major certified trading mandi in Maharashtra |
| **Commodity** | Commodity Option | `Tomato` (or ID `1`) | Certified perishable agricultural commodity |
| **Regular Price Entry** | Date | `2026-10-12` | Reporting date |
| | Arrival Quantity | `320.00` | Daily arrival volume in quintals |
| | Min Price | `2000.00` | Certified minimum discovery price (₹/qtl) |
| | Max Price | `2600.00` | Certified maximum discovery price (₹/qtl) |
| | Modal Price | `2300.00` | Valid modal price between Min and Max |
| **Price Spike Entry** | Date | `2026-10-14` | Reporting date for spike trigger |
| | Arrival Quantity | `150.00` | Low arrival causing scarcity spike |
| | Min Price | `4500.00` | Spike floor |
| | Max Price | `5500.00` | Spike ceiling |
| | Modal Price | `5000.00` | Spike (>100% higher than prior modal ~₹2150) |

---

## 4. Critical Journey Test Specifications

### Journey 1: Authentication as Data Entry Operator
*   **Test Case ID:** `TC-UI-01-LOGIN`
*   **Objective:** Verify that an operator with `ENTRY_USER` role can successfully sign in and see authorized dashboard controls.
*   **Pre-conditions:** App is up, user is logged out.
*   **Test Steps:**
    1. Navigate to `/login`.
    2. Input username `entry`.
    3. Input password `entry123`.
    4. Click the `Sign In` button.
*   **Expected Results:**
    *   Redirected to `/prices` or home page with welcome banner.
    *   Navbar reflects logged-in state: shows badge `entry (ENTRY_USER)` and `Logout` link.
    *   `+ New Price` navigation link is visible and accessible.

---

### Journey 2: Certified Price Record Submission with Validation
*   **Test Case ID:** `TC-UI-02-ADD-RECORD`
*   **Objective:** Verify that an authenticated entry user can submit a valid daily market price filing with server-side validation.
*   **Pre-conditions:** Logged in as `entry`.
*   **Test Steps:**
    1. Navigate to `/prices/new`.
    2. Select Market (e.g. `Vashi APMC`).
    3. Select Commodity (e.g. `Tomato`).
    4. Fill Date `2026-10-12`.
    5. Fill Arrival Quantity `320.00`, Min Price `2000.00`, Max Price `2600.00`, Modal Price `2300.00`.
    6. Click `Save Record`.
*   **Expected Results:**
    *   Redirected to `/prices`.
    *   Flash banner indicates successful creation (e.g. `Price record #... created successfully`).
    *   The newly created record is displayed in the price table with matching figures.

---

### Journey 3: Multi-Parameter Price Directory Search and Filter
*   **Test Case ID:** `TC-UI-03-SEARCH-FILTER`
*   **Objective:** Verify that users can filter records by commodity, mandi, and date range with sorting.
*   **Pre-conditions:** Seeded records exist in database.
*   **Test Steps:**
    1. Navigate to `/prices`.
    2. Select Commodity dropdown filter = `Tomato`.
    3. Enter From Date = `2026-10-01` and To Date = `2026-10-15`.
    4. Select Sort By = `Date`, Order = `Desc`.
    5. Click `Filter`.
*   **Expected Results:**
    *   URL includes filter parameters `commodityId`, `from`, `to`.
    *   All table rows returned correspond to commodity `Tomato`.
    *   Record dates fall strictly within the requested interval.
    *   Pagination controls and total count reflect filtered subset.

---

### Journey 4: Dashboard Summary Indicators and Drill-down Exploration
*   **Test Case ID:** `TC-UI-04-DASHBOARD-DRILLDOWN`
*   **Objective:** Verify that dashboard summarizes today's commodity trends (B08-B10) and provides functional drill-down navigation (B11) to record level.
*   **Pre-conditions:** App contains active price data.
*   **Test Steps:**
    1. Navigate to `/` (Home Dashboard).
    2. Verify key KPI stat cards (e.g., Active Mandis, Tracked Commodities, Records Live).
    3. Locate the `Today's Commodity Summary & Trends` section.
    4. Verify commodity widget displays average modal price and % change indicator.
    5. Click the `🔍 Drill-down Records` button on a commodity card.
*   **Expected Results:**
    *   Browser navigates to `/prices` with matching `commodityId` and date parameters pre-populated.
    *   Table displays the granular market filings that contributed to that summary indicator.

---

### Journey 5: Surveillance Anomaly Trigger & Alerts Feed Inspection
*   **Test Case ID:** `TC-UI-05-ALERT-SURVEILLANCE`
*   **Objective:** Verify that an extreme day-over-day price jump (>15% threshold breach) triggers an exception alert visible on the `/alerts` dashboard.
*   **Pre-conditions:** Logged in as `entry`.
*   **Test Steps:**
    1. Navigate to `/prices/new`.
    2. Enter a record for `Tomato` on date `2026-10-14` with a spiked modal price `5000.00` (exceeding configured 15% threshold vs baseline).
    3. Submit the record.
    4. Navigate to `/alerts`.
*   **Expected Results:**
    *   The alerts dashboard displays the active rule banner (`Threshold: 15%`).
    *   Under `Active Spikes & Volatility Breaches`, an alert card appears for `Tomato`.
    *   Card highlights the percentage increase (`+...% Spike`) and prior comparison price.

---

## 5. Test Execution & Reporting
*   **Execution Commands:**
    *   Unit Tests only: `mvn test`
    *   Selenium UI Suite: `mvn verify -Pui-tests` (or `mvn verify -Pui-tests -Dbase.url=http://localhost:8081/ampt`)
*   **Reports:**
    *   Surefire / Failsafe XML/HTML test reports in `target/failsafe-reports/`.
    *   Failure screenshots in `target/screenshots/`.
