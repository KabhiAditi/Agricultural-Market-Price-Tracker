# Pull Request: Feature 1 (Core Workflow: Data Entry, Role-Based Access & Price Directory Search)

- **Source Branch:** `feature/B03-price-entry`
- **Target Branch:** `develop`
- **Type:** `feat`
- **Related Backlog Items:** Closes #B03, Closes #B04, Closes #B05, Closes #B06, Closes #B07

---

## 1. Description of Changes
This pull request introduces the first end-to-end user workflows for the **Agricultural Market Price Tracker (AMPT)**:
1. **B03 — Price Entry Form (Create)**: Full server-side validated web form for recording daily arrivals, min, max, and modal price figures per market and commodity.
2. **B04 — Edit/Correct Existing Record (Update)**: Pre-populated edit interface updating price and arrival records in place, maintaining `updated_at` audit timestamps.
3. **B05 — Role-Based Access Control**: Simple session-based authentication backed by BCrypt password hashing. Seeded credentials:
   - `entry` (`entry123`) -> `Role.ENTRY_USER` (authorized for create/edit).
   - `viewer` (`viewer123`) -> `Role.VIEWER` (read-only; rejected with 403 on create/edit).
4. **B06 — Price Search & Filter Directory**: Publicly accessible search interface filtering across markets, commodities, and date ranges (`from` / `to`).
5. **B07 — Pagination & Sorting**: Spring Data `Pageable` displaying 10 records per page with sort options (date, modal price, arrival qty; asc/desc).
6. **Realistic Data Seeding**: Initialized with 3 APMC mandis, 3 staple commodities, and 14 days of realistic price records (126 entries).

---

## 2. Review Notes & Self-Audit Fixes Applied
During peer self-review (documented in [`docs/evidence/task05/review-notes.md`](review-notes.md)), 3 key improvements were identified and resolved in commit `8c1404f`:
1. **Date Range Sanity**: Graceful automatic sorting when `from` date exceeds `to` date.
2. **Flat Price Boundary Check**: Handled uniform price conditions where `minPrice == maxPrice` requires `modalPrice == minPrice`.
3. **HTTP 403 Headers**: Explicit UTF-8 error payload on unauthorized access attempts.

---

## 3. Verification & Test Evidence
- **Unit Tests (`PriceRecordServiceTest`)**: 9 test cases covering form validation, edge cases, and CRUD operations.
- **Controller Tests (`PriceRecordControllerTest`)**: 7 MockMvc test cases validating public search, authentication redirects, 403 authorization gating, and form redisplay on invalid inputs.
- **Auth Tests (`AuthControllerTest`)**: 4 MockMvc test cases validating login, logout, and credential matching.
- **Total Test Suite**: 21 passed, 0 failures, 0 errors.
- **Artifact Packaging**: `mvn clean package` successfully generated executable `target/ampt.war`.
