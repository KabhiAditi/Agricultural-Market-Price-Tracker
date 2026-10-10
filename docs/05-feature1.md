# Task 5 — Feature 1 with Branching (Data Entry, Search & Role Check)

## 1. Executive Summary
In **Task 5**, the core business workflow of the **Agricultural Market Price Tracker (AMPT)** was developed on an isolated feature branch (`feature/B03-price-entry`) following the **Git Flow** methodology. The implementation delivers user stories **B03, B04, B05, B06, and B07**, backed by automated unit and MockMvc test suites, peer self-review comments, and a non-fast-forward merge into `develop`.

---

## 2. Implemented Backlog User Stories

| Backlog ID | Area | Feature Capability | Implementation Details |
|---|---|---|---|
| **B03** | Data Entry | Price-entry form (create) with validation | `PriceRecordForm`, `PriceRecordService.validateForm()`, server-side validation ensuring positive figures, required fields, and modal price bound between `minPrice` and `maxPrice`. |
| **B04** | Data Entry | Edit/correct existing price record (update) | `GET /prices/{id}/edit` and `POST /prices/{id}`, pre-populated form, preserving database ID and updating `updated_at` audit timestamp. |
| **B05** | Security | Role-based access control (RBAC) | BCrypt password hashing (`BCryptPasswordEncoder`), session-based authentication (`AuthController`, `AuthService`), and `RoleCheckInterceptor` enforcing `Role.ENTRY_USER` on write operations while keeping search public. |
| **B06** | Dashboard | Search & Filter price directory | Dynamic Spring Data JPA Specification filtering by Market APMC, Commodity, and reporting Date Range (`from` / `to`). |
| **B07** | Dashboard | Paginated & sortable results | Spring Data `Pageable` displaying 10 records per page with sorting by Date, Modal Price, or Arrival Quantity (Ascending/Descending). |

---

## 3. Architecture & Key Components

### 3.1 Security & Role Check Layer
- **Seeded User Credentials**:
  - `entry` / `entry123` -> Assigned `Role.ENTRY_USER` (Permitted to view, search, create, and edit price records).
  - `viewer` / `viewer123` -> Assigned `Role.VIEWER` (Read-only access; attempts to access `/prices/new` or edit return HTTP 403 Forbidden).
- **Session Management**: Successful authentication stores `AppUser` in `session.setAttribute("currentUser", user)`.
- **`RoleCheckInterceptor`**: Pre-handles requests to `/prices/new`, `/prices/*/edit`, and `POST /prices*`. Unauthenticated requests redirect to `/login?redirect=...`; non-entry roles receive HTTP 403.

### 3.2 Service Layer & Domain Validation
- **Validation Rules**:
  1. Minimum price must not exceed maximum price (`minPrice <= maxPrice`).
  2. Modal price must lie between min and max price (`minPrice <= modalPrice <= maxPrice`).
  3. When minimum price equals maximum price, modal price must equal both (`minPrice == maxPrice == modalPrice`).
  4. Arrival quantity and prices must be positive.

### 3.3 Realistic Seed Data
The `DataInitializer` seeds:
- 3 Mandis: Nagpur APMC, Pune APMC, Indore Mandi.
- 3 Commodities: Tomato, Onion, Wheat.
- 14 Consecutive Days of price records (2026-09-27 to 2026-10-10) generating **126 realistic price records**.

---

## 4. Test Verification & Packaging

The application is thoroughly verified with 21 automated unit and integration tests:
- **`PriceRecordServiceTest` (9 tests)**: Validates boundary price rules, uniform flat pricing, min > max rejection, modal out of bounds, and repository CRUD invocations.
- **`PriceRecordControllerTest` (7 tests)**: MockMvc testing of public directory access, redirect to `/login` for unauthenticated requests, 403 Forbidden for `VIEWER` role, valid creation redirects, and form redisplay upon validation errors.
- **`AuthControllerTest` (4 tests)**: MockMvc testing of login view rendering, successful login session attribution, invalid credential flash errors, and session invalidation on logout.
- **`AmptApplicationTests` (1 test)**: Spring context and persistence bootstrapping verification.

**Build Status:**
```text
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
[INFO] Building war: C:\...\target\ampt.war
[INFO] BUILD SUCCESS
```

---

## 5. Peer Self-Review & Fix Audit

As documented in [`docs/evidence/task05/review-notes.md`](evidence/task05/review-notes.md), 3 code review observations were documented and resolved prior to merging:
1. **Inverted Date Range Handling**: Automatically re-orders `from` and `to` dates if inverted by user and presents informative notice.
2. **Uniform Flat Price Boundary**: Explicitly verified that `minPrice == maxPrice == modalPrice` is treated as valid.
3. **HTTP 403 Header & Encoding**: Set explicit UTF-8 text response payload on forbidden operations.

---

## 6. Git Branching & Merge Evidence

The branch was developed using atomic conventional commits and merged into `develop` using a non-fast-forward merge (`--no-ff`):

```text
*   6e1d9c2 (HEAD -> develop) Merge branch 'feature/B03-price-entry' into develop
|\  
| * 8c1404f (feature/B03-price-entry) fix(review): address self-review comments for inverted date ranges and uniform pricing
| * 2fe6c82 test(prices): add unit tests for PriceRecordService and MockMvc controller suites
| * d2a37a8 feat(ui): implement price entry, edit, search views with pagination and sorting (B03, B04, B06, B07)
| * d8611d0 feat(prices): add price record form DTO, service layer with validation, and seed data (B03, B04)
| * 2e68fc2 feat(security): add bcrypt hashing, session auth service and role interceptor (B05)
|/  
* 78f74a3 docs(evidence): capture task 4 git logs, branch list, and remotes
```

---

## 7. Application Verification & Screenshots Checklist

When running the application (`mvn spring-boot:run` on port `8082`), capture the following verification screens:

1. **Dashboard Overview** (`http://localhost:8082/`): Shows reporting markets (3), tracked commodities (3), and live records (126).
2. **Login View** (`http://localhost:8082/login`): Displays credential helper table (`entry`/`entry123`, `viewer`/`viewer123`).
3. **Public Search Directory** (`http://localhost:8082/prices`): Demonstrates filtering by APMC mandi and commodity, 10 records per page pagination, and sort toggles.
4. **New Price Entry Form** (`http://localhost:8082/prices/new`): Authenticated as `entry` user, showing form inputs and dropdowns.
5. **Server-Side Validation Failure**: Enter `Min Price = 2000`, `Max Price = 2500`, and `Modal Price = 3000` to show the rejection message: *"Modal price cannot be greater than maximum price."*
6. **Edit Price Record** (`http://localhost:8082/prices/1/edit`): Shows pre-populated values and update action.
7. **Role Access Gating**: Logged in as `viewer`, attempting to access `http://localhost:8082/prices/new` yields HTTP 403 Forbidden.
