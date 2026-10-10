# Task 6 — MVP Completion, Merge Conflict Resolution & Release Tagging (v1.0.0)

**Project:** Agricultural Market Price Tracker (AMPT)  
**Release Tag:** `v1.0.0` (Annotated Git Tag)  
**Target Milestone:** MVP Release (Tasks 1 through 6 Complete)  
**Integration Branch:** `develop`  
**Production Release Branch:** `main`  
**Date:** October 10, 2026  

---

## 1. Executive Summary

This release marks the **formal completion of the Agricultural Market Price Tracker (AMPT) Minimum Viable Product (MVP)**. All 15 backlog user stories (**B01 through B15**) spanning Foundation, Data Entry, Searchable Dashboard, Summary Indicators, Trend Drill-Downs, and Automated Market Volatility & Gap Surveillance have been fully implemented, rigorously unit- and integration-tested (33/33 tests passing), and packaged into a deployable standalone Spring Boot WAR archive (`ampt.war`).

In addition to feature delivery, Task 6 executed a production-grade **DevOps branching and merge conflict resolution scenario**:
1. Two feature branches (`feature/B08-summary-indicators` and `feature/B13-alerts`) were branched concurrently from `develop`.
2. Both branches deliberately implemented modifications to overlapping files (`src/main/resources/templates/dashboard.html` and `src/main/java/com/ampt/repository/PriceRecordRepository.java`).
3. Branch 1 merged cleanly into `develop`.
4. Branch 2 triggered a genuine 3-way merge conflict upon integration.
5. The conflict markers were systematically inspected, resolved by preserving and unifying both feature capabilities, committed to `develop`, and validated through a complete Maven clean build.
6. The validated `develop` branch was merged into `main` and tagged with annotated tag `v1.0.0`.

---

## 2. Complete Backlog Traceability (B01–B15: 100% Done)

| ID | Module / Area | Backlog Item | Priority | Points | Implemented Branch / Task | Status |
|:---|:---|:---|:---:|:---:|:---|:---:|
| **B01** | Foundation | Relational data model (`Market`, `Commodity`, `PriceRecord`, `AppUser`, `Role`) | High | 3 | `main` / Task 3 | **Done** |
| **B02** | Foundation | Project skeleton, Maven configuration, H2 & MySQL dual profiles, data seeding | High | 3 | `main` / Task 3 | **Done** |
| **B03** | Data Entry | Price-entry form (create) with validation (min &le; modal &le; max, positive prices) | High | 3 | `feature/B03-price-entry` / Task 5 | **Done** |
| **B04** | Data Entry | Edit/correct existing price record (update) with audit timestamps | High | 2 | `feature/B03-price-entry` / Task 5 | **Done** |
| **B05** | Security | Role check & session authentication (BCrypt hashing, `ENTRY_USER` vs `VIEWER`) | Med | 2 | `feature/B03-price-entry` / Task 5 | **Done** |
| **B06** | Dashboard | Dynamic search engine (filter by market, commodity, date range) | High | 4 | `feature/B03-price-entry` / Task 5 | **Done** |
| **B07** | Dashboard | Paginated (10/page) and sortable (`recordDate`, `modalPrice`) results list | Med | 2 | `feature/B03-price-entry` / Task 5 | **Done** |
| **B08** | Summary | Compute daily modal and average prices per commodity across reporting mandis | High | 3 | `feature/B08-summary-indicators` / Task 6 | **Done** |
| **B09** | Summary | Daily percentage change vs previous reporting day with "no prior data" fallback | High | 3 | `feature/B08-summary-indicators` / Task 6 | **Done** |
| **B10** | Summary | Modern dashboard indicator widgets displaying live prices, volumes, and trends | Med | 2 | `feature/B08-summary-indicators` / Task 6 | **Done** |
| **B11** | Drill-Down | Widget drill-down click-through to underlying filtered record set (`/prices`) | Med | 2 | `feature/B08-summary-indicators` / Task 6 | **Done** |
| **B12** | Drill-Down | Chronological single-commodity price history and trend table (`/prices/history`) | Med | 3 | `feature/B08-summary-indicators` / Task 6 | **Done** |
| **B13** | Surveillance | Configurable anomaly rule: flag movements &ge; 15% (`ampt.alert.threshold-percent`) | High | 3 | `feature/B13-alerts` / Task 6 | **Done** |
| **B14** | Surveillance | Missing data rule: flag mandis with no filing after 17:00 cutoff (`ampt.alert.cutoff-hour`) | Med | 2 | `feature/B13-alerts` / Task 6 | **Done** |
| **B15** | Surveillance | Dedicated alerts surveillance dashboard (`/alerts`) and REST API (`/api/alerts`) | Med | 2 | `feature/B13-alerts` / Task 6 | **Done** |

**Total Story Points Completed: 39 / 39 (100% Velocity Achieved)**

---

## 3. Parallel Feature Branching Architecture

In accordance with `docs/BRANCHING.md`, development proceeded on two isolated feature branches originating from `develop`:

```
                 develop (Integration)
                   |
     +-------------+-------------+
     |                           |
feature/B08-summary-indicators   feature/B13-alerts
     |                           |
   B08-B12 Summary & Trends     B13-B15 Anomaly Rules & Alerts
     |                           |
   Commit 42aeacb              Commit 1cd26a6
     |                           |
     +--- Merge cleanly (e291150)|
     |                           |
     +--- Merge into develop ----+
                 |
      [MERGE CONFLICT TRIGGERED]
                 |
         Resolved in 0c4cae2
                 |
            develop clean
                 |
            Merged to main
                 |
             Tag v1.0.0
```

### Feature Branch 1: `feature/B08-summary-indicators`
- **Scope**: B08, B09, B10, B11, B12.
- **Key Deliverables**:
  - `SummaryService.java`: Computes commodity modal prices, average modal prices, and percentage changes vs the previous reporting day. Implements clean fallback when no prior day records exist (`hasPriorData = false`).
  - `CommoditySummaryDto.java`: Data transfer object encapsulating today's avg modal price, prior day price, % movement, reporting mandi count, and arrival volume.
  - `PriceRecordController.java` (`/prices/history`): Full chronological price history view for any commodity across markets.
  - `templates/prices/history.html`: Dedicated historical trend table view with commodity filter and price stats.
  - `templates/dashboard.html`: Live summary widgets under the main hero section with direct drill-down links to `/prices` and `/prices/history`.
  - Tests: `SummaryServiceTest.java` (3 test cases), updated `PriceRecordControllerTest.java`.

### Feature Branch 2: `feature/B13-alerts`
- **Scope**: B13, B14, B15.
- **Key Deliverables**:
  - `application.yml`: Configurable threshold properties:
    ```yaml
    ampt:
      alert:
        threshold-percent: 15
        cutoff-hour: 17
    ```
  - `AlertItemDto.java`: Represents both `THRESHOLD_BREACH` and `MISSING_DATA` exceptions with severity levels (`HIGH`, `MEDIUM`), price diffs, and descriptive explanations.
  - `AlertService.java`:
    - Evaluates price records on creation/edit against prior reporting day; sets `record.isAlert()` when price movement &ge; 15%.
    - Evaluates reporting mandis against expected daily filings after cutoff time (17:00 IST).
    - Exposes `getAllActiveAlerts()`, `getThresholdBreachAlerts()`, and `getMissingDataAlerts()`.
  - `AlertController.java`: Thymeleaf controller for `/alerts` with tab filters (`all`, `threshold`, `missing`) and REST endpoint `/api/alerts`.
  - `templates/alerts/index.html`: Dedicated surveillance center displaying active anomalies, metric badges, severity cards, and configuration threshold banners.
  - `templates/dashboard.html`: Added ⚠️ Alerts link to header navigation and active exception alert cards.
  - Tests: `AlertServiceTest.java` (6 unit tests), `AlertControllerTest.java` (2 MockMvc tests).

---

## 4. The Merge Conflict Scenario & Resolution

### How the Conflict Occurred
To demonstrate a genuine, non-trivial Git conflict:
1. `feature/B08-summary-indicators` inserted the Summary Indicators section (`<!-- B10: Summary Indicator Widgets & B11: Drill-down Links -->`) immediately following `<div class="stats-grid">...</div>` (line 186) in `src/main/resources/templates/dashboard.html`.
2. Concurrently on `feature/B13-alerts`, the Active Market Alerts section (`<!-- B13-B15: Active Market Alerts & Exceptions Overview -->`) was inserted at the exact same location (line 186) in `src/main/resources/templates/dashboard.html`.
3. In `PriceRecordRepository.java`, both branches independently declared new query methods for prior record lookups and daily filings.

### Git Conflict Output
When merging the second branch (`feature/B13-alerts`) into `develop`:
```bash
$ git merge --no-ff feature/B13-alerts
Auto-merging src/main/java/com/ampt/repository/PriceRecordRepository.java
CONFLICT (content): Merge conflict in src/main/java/com/ampt/repository/PriceRecordRepository.java
Auto-merging src/main/resources/templates/dashboard.html
CONFLICT (content): Merge conflict in src/main/resources/templates/dashboard.html
Automatic merge failed; fix conflicts and then commit the result.
```

### Resolution Strategy
1. **In `PriceRecordRepository.java`**: Kept all required queries from both branches:
   - `findTopByCommodity_IdAndRecordDateLessThanOrderByRecordDateDesc` (needed for B08-B09 summary computations).
   - `findByRecordDate` (needed for B14 missing data analysis).
2. **In `templates/dashboard.html`**: Logically integrated both components:
   - Preserved navigation bar link for `⚠️ Alerts`.
   - Placed the **Active Market Alerts & Exceptions banner** prominently below the quick stats to give immediate visibility into market volatility.
   - Placed the **Commodity Summary & Trends Widgets** with Drill-Down and History buttons directly beneath the alerts banner.
3. The merge was staged with `git add` and committed with commit message:
   `Merge branch 'feature/B13-alerts' into develop (resolved merge conflict in dashboard.html and PriceRecordRepository.java)`.
4. Full raw console transcript saved to `docs/evidence/task06/conflict-log.txt`.

---

## 5. Automated Build & Test Validation

Before promoting `develop` to `main`, the complete test and build pipeline was executed:

```bash
$ mvn clean package
```

### Build Results Summary
- **Compilation**: 100% clean, 0 warnings.
- **Unit & Integration Test Suites**:
  - `AuthControllerTest`: 4 tests, 0 failures.
  - `PriceRecordControllerTest`: 8 tests, 0 failures.
  - `AlertControllerTest`: 2 tests, 0 failures.
  - `AlertServiceTest`: 6 tests, 0 failures.
  - `PriceRecordServiceTest`: 9 tests, 0 failures.
  - `SummaryServiceTest`: 3 tests, 0 failures.
  - `AmptApplicationTests`: 1 context load test, 0 failures.
- **Total Tests Run**: **33**
- **Failures / Errors / Skipped**: **0 / 0 / 0**
- **Artifact Produced**: `target/ampt.war` (Standalone executable and servlet-container ready archive).
- **Build Status**: **BUILD SUCCESS** (Execution time: ~32s).

---

## 6. Release Tagging & Promotion

Following standard Gitflow practice:
1. `develop` was merged into `main` using a clean merge commit.
2. An annotated Git tag `v1.0.0` was created on `main`:
   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0: Agricultural Market Price Tracker MVP (Tasks 1-6 complete)"
   ```
3. Verification of created tags:
   ```bash
   git tag -n
   # v1.0.0          Release v1.0.0: Agricultural Market Price Tracker MVP (Tasks 1-6 complete)
   ```

---

## 7. How to Run and Verify Every MVP Feature

### Starting the Application
```bash
# Run with local H2 database profile (port 8082)
mvn spring-boot:run
# Or launch the packaged WAR
java -jar target/ampt.war
```

Access the application in any browser at: `http://localhost:8082/`

### Test Credentials
- **Mandi Data Entry Official**: `entry` / `entry123` (Role: `ENTRY_USER`)
- **Public Viewer**: `viewer` / `viewer123` (Role: `VIEWER`)

### Feature Verification Matrix

| Feature | URL Path | Verification Steps |
|:---|:---|:---|
| **Dashboard & Widgets (B08, B10)** | `GET /` | Observe live summary cards for Tomato, Onion, Soybean showing average modal prices and 24h % change. |
| **Widget Drill-Down (B11)** | Click **🔍 Drill-down Records** on Tomato card | Navigates to `/prices?commodityId=1&from=...&to=...` displaying underlying mandi prices for that commodity. |
| **Price History Trend (B12)** | Click **📈 History** or `GET /prices/history?commodityId=1` | View chronological list of Tomato prices across Nagpur, Pune, and Nashik APMC. |
| **Search & Pagination (B06, B07)** | `GET /prices` | Filter by Mandi, Commodity, Date range; change sorting and page through results. |
| **Data Entry Validation (B03, B04)** | `GET /prices/new` (Login as `entry`) | Attempt invalid entry (modal > max); verify validation rejection. Submit valid entry; verify immediate presence in search. |
| **Role Enforcement (B05)** | `GET /prices/new` (Login as `viewer`) | Verify redirect or 403 Forbidden preventing unauthorized price entry. |
| **Alerts & Exceptions (B13-B15)** | `GET /alerts` | View flagged price movements (&ge; 15%) and missing mandi filings. Test tab filters and JSON API at `GET /api/alerts`. |

---

## 8. Evidence Files

All Task 6 evidence files are archived in `docs/evidence/task06/`:
- `conflict-log.txt`: Complete terminal log showing `git checkout`, `git merge`, `CONFLICT` markers, resolution edits, `git add`, `git commit`, `git status`, and `git log --graph --oneline`.
- Release Tag: `v1.0.0` on `main`.

---
*MVP Release v1.0.0 approved and signed off. Ready for CI/CD pipeline automation (Tasks 7–15).*
