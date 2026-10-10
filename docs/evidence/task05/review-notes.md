# Task 5 — Code Review & Self-Audit Notes

## Pull Request: Feature B03/B04/B05/B06/B07 (`feature/B03-price-entry` -> `develop`)
**Reviewer:** DevOps Engineer (Self-Review)  
**Date:** 2026-10-10  
**Branch:** `feature/B03-price-entry`

---

### Review Comment 1 (Logic / UX) — Date Range Validation on Search Filter
- **File:** `src/main/java/com/ampt/controller/PriceRecordController.java`
- **Line:** `searchPrices()` method
- **Finding:** If a user specifies a `from` date that is chronologically after the `to` date (e.g., `from=2026-10-10` and `to=2026-09-01`), the JPA Specification applies both predicates (`recordDate >= from AND recordDate <= to`), resulting in an empty dataset without informing the user of the invalid range.
- **Recommendation:** Add defensive validation in `PriceRecordController`: if `from != null && to != null && from.isAfter(to)`, swap the dates gracefully or attach a warning flash message explaining that the date filter was inverted.
- **Action Taken:** Updated controller to automatically correct inverted date ranges (`from` and `to` swapped) and notify the user via a UI flash warning.

---

### Review Comment 2 (Domain Validation) — Flat Pricing Edge Case (Min == Max)
- **File:** `src/main/java/com/ampt/service/PriceRecordService.java`
- **Line:** `validateForm()` method
- **Finding:** In agricultural spot markets, homogeneous commodity lots on quiet days can trade at a single uniform rate where `minPrice == modalPrice == maxPrice`. While our inequalities `<` and `>` allow equality, we should add an explicit check: if `minPrice.compareTo(maxPrice) == 0`, then `modalPrice` must strictly match `minPrice`.
- **Recommendation:** Document and test the boundary condition where `minPrice == modalPrice == maxPrice` explicitly in unit tests and service logic to prevent edge case discrepancies.
- **Action Taken:** Added boundary assertion in `PriceRecordService` and added dedicated unit test `testValidateForm_UniformFlatPrice()` in `PriceRecordServiceTest`.

---

### Review Comment 3 (Security & HTTP Semantics) — Access Denied Error Clarity
- **File:** `src/main/java/com/ampt/interceptor/RoleCheckInterceptor.java`
- **Line:** `preHandle()` authorization check
- **Finding:** When an authenticated user with `Role.VIEWER` tries to access `/prices/new` or submit a `POST /prices`, sending `response.sendError(403)` with a default container message might result in an unstyled error page depending on the servlet container.
- **Recommendation:** Ensure the error response explicitly includes the forbidden error message in response header and body, and ensure the URL matching handles paths with context paths or query parameters without leaking entry endpoints.
- **Action Taken:** Hardened URI matching using Spring `AntPathMatcher` logic and set `response.setStatus(HttpServletResponse.SC_FORBIDDEN)` with explicit Content-Type and UTF-8 error payload.

---

### Review Conclusion
All 3 review findings have been resolved with targeted code updates, verified with green JUnit test runs, and committed on `feature/B03-price-entry` prior to merging into `develop`.
