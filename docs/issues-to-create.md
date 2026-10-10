# GitHub Issues Specification: Backlog Items B01–B15

This document contains the complete specifications for all 15 Agile User Stories defined in the product backlog (`02-agile-planning-devops-workflow.md`).

You can copy-paste each item directly into the [GitHub Issues tab](https://github.com/KabhiAditi/Agricultural-Market-Price-Tracker/issues/new), or run the automated script provided at the end of this document.

---

## Issue B01
- **Title**: `[B01] Define data model (Market, Commodity, PriceRecord, AppUser/Role)`
- **Labels**: `enhancement`, `foundation`, `priority:high`
- **Story Points**: `3`
- **Area**: Foundation

### Description
> **As a** developer,  
> **I want** a clear data model for markets, commodities, price records and users,  
> **So that** every later feature has a stable schema to build on.

### Acceptance Criteria
- [ ] **AC1**: Given the schema is applied, when I inspect the DB, then `Market`, `Commodity`, `PriceRecord`, and `AppUser` tables exist with foreign keys linking `PriceRecord` -> `Market` and `PriceRecord` -> `Commodity`.
- [ ] **AC2**: Given a `PriceRecord`, when it is saved, then `date`, `min_price`, `max_price`, `modal_price`, and `arrival_qty` are all required fields.

---

## Issue B02
- **Title**: `[B02] Project skeleton, build tool config, local DB schema/migration`
- **Labels**: `task`, `foundation`, `priority:high`
- **Story Points**: `3`
- **Area**: Foundation

### Description
> **As a** developer,  
> **I want** a runnable local skeleton,  
> **So that** I can start building features immediately.

### Acceptance Criteria
- [ ] **AC1**: Given a fresh clone, when I run the build command (`mvn clean package`), then the app compiles and starts without manual intervention.
- [ ] **AC2**: Given the skeleton, when I check the repo, then build-tool config (`pom.xml`), folder structure, and DB connection config (`application.yml`) are all present.

---

## Issue B03
- **Title**: `[B03] Price-entry form (create) with validation`
- **Labels**: `enhancement`, `data-entry`, `priority:high`
- **Story Points**: `3`
- **Area**: Entry

### Description
> **As a** mandi official,  
> **I want** to enter a new price record,  
> **So that** today's price is available to everyone searching the app.

### Acceptance Criteria
- [ ] **AC1**: Given valid market/commodity/price/quantity values, when I submit the form, then a new `PriceRecord` is created and visible in search within the same session.
- [ ] **AC2**: Given a missing required field, when I submit, then the form is rejected with a field-level error and no record is created.

---

## Issue B04
- **Title**: `[B04] Edit/correct existing price record (update)`
- **Labels**: `enhancement`, `data-entry`, `priority:high`
- **Story Points**: `2`
- **Area**: Entry

### Description
> **As a** mandi official,  
> **I want** to correct a record I entered,  
> **So that** mistakes don't stay live.

### Acceptance Criteria
- [ ] **AC1**: Given an existing record I own, when I edit and save valid values, then the record updates in place and an `updated_at` timestamp changes.
- [ ] **AC2**: Given invalid edited values, when I save, then the update is rejected and the original record is unchanged.

---

## Issue B05
- **Title**: `[B05] Role check — only authorized roles enter/edit`
- **Labels**: `security`, `data-entry`, `priority:medium`
- **Story Points**: `2`
- **Area**: Entry

### Description
> **As a** system,  
> **I want** only authorized roles to create/edit price data,  
> **So that** data integrity is protected.

### Acceptance Criteria
- [ ] **AC1**: Given an unauthenticated or read-only user, when they call create/edit endpoints, then the request is rejected with HTTP 403 Forbidden.
- [ ] **AC2**: Given an authorized data-entry user, when they call the same endpoints, then the request succeeds.

---

## Issue B06
- **Title**: `[B06] Search API + UI (market, commodity, date range)`
- **Labels**: `enhancement`, `dashboard`, `priority:high`
- **Story Points**: `4`
- **Area**: Dashboard

### Description
> **As any** user,  
> **I want** to search prices by market, commodity, and date range,  
> **So that** I can find the exact data I need.

### Acceptance Criteria
- [ ] **AC1**: Given seed data across 3+ markets, when I search by commodity only, then all matching records across markets are returned.
- [ ] **AC2**: Given a date range with no data, when I search, then an empty-state message is shown (not an error).

---

## Issue B07
- **Title**: `[B07] Paginated, sortable results list`
- **Labels**: `enhancement`, `dashboard`, `priority:medium`
- **Story Points**: `2`
- **Area**: Dashboard

### Description
> **As any** user,  
> **I want** results paginated and sortable,  
> **So that** large result sets are usable.

### Acceptance Criteria
- [ ] **AC1**: Given more than one page of results, when I navigate to page 2, then the correct next set of records is shown.
- [ ] **AC2**: Given a sort-by-price request, when applied, then results are ordered ascending/descending as selected.

---

## Issue B08
- **Title**: `[B08] Compute today's avg/modal price per commodity`
- **Labels**: `enhancement`, `analytics`, `priority:high`
- **Story Points**: `3`
- **Area**: Summary

### Description
> **As any** user,  
> **I want** to see today's average/modal price per commodity,  
> **So that** I get a quick read without searching.

### Acceptance Criteria
- [ ] **AC1**: Given records for a commodity today, when the dashboard loads, then the computed modal price matches a manual recalculation on the same data.

---

## Issue B09
- **Title**: `[B09] Compute % change vs previous reporting day`
- **Labels**: `enhancement`, `analytics`, `priority:high`
- **Story Points**: `3`
- **Area**: Summary

### Description
> **As a** farmer,  
> **I want** to see how much a price moved since yesterday,  
> **So that** I can judge whether to sell now.

### Acceptance Criteria
- [ ] **AC1**: Given yesterday's and today's modal price, when displayed, then % change is computed as `(today - yesterday) / yesterday * 100`, signed correctly.
- [ ] **AC2**: Given no record for yesterday, when displayed, then the UI shows "no prior data" instead of a broken calculation.

---

## Issue B10
- **Title**: `[B10] Dashboard indicator widgets`
- **Labels**: `enhancement`, `ui`, `priority:medium`
- **Story Points**: `2`
- **Area**: Summary

### Description
> **As any** user,  
> **I want** indicators on the dashboard home,  
> **So that** I see the market health at a glance.

### Acceptance Criteria
- [ ] **AC1**: Given the dashboard loads, when data exists, then at least "today's avg price", "% change", and "markets reporting" widgets render with live values.

---

## Issue B11
- **Title**: `[B11] Click-through from indicator to record list`
- **Labels**: `enhancement`, `ui`, `priority:medium`
- **Story Points**: `2`
- **Area**: Drill-down

### Description
> **As any** user,  
> **I want** to click an indicator to see the underlying records,  
> **So that** I can verify or investigate a number.

### Acceptance Criteria
- [ ] **AC1**: Given I click a summary widget, when the drill-down view opens, then it shows exactly the record set used to compute that widget.

---

## Issue B12
- **Title**: `[B12] Single-record / full-history detail view`
- **Labels**: `enhancement`, `ui`, `priority:medium`
- **Story Points**: `3`
- **Area**: Drill-down

### Description
> **As any** user,  
> **I want** a full history for one market+commodity,  
> **So that** I can see the trend over time.

### Acceptance Criteria
- [ ] **AC1**: Given a market+commodity pair with 5+ historical records, when I open detail view, then all records are listed in date order.

---

## Issue B13
- **Title**: `[B13] Rule: flag price change beyond threshold`
- **Labels**: `enhancement`, `alerts`, `priority:high`
- **Story Points**: `3`
- **Area**: Alerts

### Description
> **As a** farmer,  
> **I want** to be alerted when a price moves sharply,  
> **So that** I don't miss a good/bad moment to sell.

### Acceptance Criteria
- [ ] **AC1**: Given a configurable threshold (e.g., 15%), when a new record's % change exceeds it, then the record is flagged as an alert.
- [ ] **AC2**: Given a change below threshold, when saved, then no alert is created.

---

## Issue B14
- **Title**: `[B14] Rule: flag market missing today's data`
- **Labels**: `enhancement`, `alerts`, `priority:medium`
- **Story Points**: `2`
- **Area**: Alerts

### Description
> **As a** market analyst,  
> **I want** to know when a market hasn't reported today,  
> **So that** gaps in coverage are visible.

### Acceptance Criteria
- [ ] **AC1**: Given a market with no record by the configured cutoff time, when the rule runs, then that market appears in the exceptions list.

---

## Issue B15
- **Title**: `[B15] Alerts/exceptions list view`
- **Labels**: `enhancement`, `alerts`, `priority:medium`
- **Story Points**: `2`
- **Area**: Alerts

### Description
> **As any** user,  
> **I want** a dedicated alerts view,  
> **So that** exceptions aren't buried in the normal dashboard.

### Acceptance Criteria
- [ ] **AC1**: Given active alerts exist, when I open the alerts view, then all threshold-breach and missing-data exceptions are listed, most-recent first.

---

## Automated Script for GitHub CLI or REST API

If you have a GitHub Personal Access Token (`$env:GITHUB_TOKEN`), you can run the following PowerShell command to create all 15 issues automatically:

```powershell
$token = $env:GITHUB_TOKEN # or replace with "ghp_yourTokenHere"
$repo = "KabhiAditi/Agricultural-Market-Price-Tracker"

$issues = @(
  @{ title = "[B01] Define data model (Market, Commodity, PriceRecord, AppUser/Role)"; body = "### Backlog Item B01`n**Area**: Foundation | **Points**: 3 | **Priority**: High`n`n> As a developer, I want a clear data model for markets, commodities, price records and users, so that every later feature has a stable schema to build on.`n`n- **AC1:** Market, Commodity, PriceRecord, AppUser tables exist with FKs linking PriceRecord to Market and Commodity.`n- **AC2:** PriceRecord requires date, min_price, max_price, modal_price, and arrival_qty." },
  @{ title = "[B02] Project skeleton, build tool config, local DB schema/migration"; body = "### Backlog Item B02`n**Area**: Foundation | **Points**: 3 | **Priority**: High`n`n> As a developer, I want a runnable local skeleton, so that I can start building features immediately.`n`n- **AC1:** 'mvn clean package' builds and starts without manual intervention.`n- **AC2:** Maven build config, folder structure, DB connection config present." },
  @{ title = "[B03] Price-entry form (create) with validation"; body = "### Backlog Item B03`n**Area**: Entry | **Points**: 3 | **Priority**: High`n`n> As a mandi official, I want to enter a new price record, so that today's price is available to everyone searching the app.`n`n- **AC1:** Valid submission creates PriceRecord visible in search.`n- **AC2:** Missing required field rejected with validation errors." },
  @{ title = "[B04] Edit/correct existing price record (update)"; body = "### Backlog Item B04`n**Area**: Entry | **Points**: 2 | **Priority**: High`n`n> As a mandi official, I want to correct a record I entered, so that mistakes don't stay live.`n`n- **AC1:** Valid edit updates in place and changes updated timestamp.`n- **AC2:** Invalid values rejected." },
  @{ title = "[B05] Role check — only authorized roles enter/edit"; body = "### Backlog Item B05`n**Area**: Entry | **Points**: 2 | **Priority**: Medium`n`n> As a system, I want only authorized roles to create/edit price data, so that data integrity is protected.`n`n- **AC1:** Unauthenticated/read-only user rejected (403).`n- **AC2:** Authorized user succeeds." },
  @{ title = "[B06] Search API + UI (market, commodity, date range)"; body = "### Backlog Item B06`n**Area**: Dashboard | **Points**: 4 | **Priority**: High`n`n> As any user, I want to search prices by market, commodity, and date range, so that I can find the exact data I need.`n`n- **AC1:** Search by commodity across multiple markets returns all matches.`n- **AC2:** Date range with no data returns clean empty-state." },
  @{ title = "[B07] Paginated, sortable results list"; body = "### Backlog Item B07`n**Area**: Dashboard | **Points**: 2 | **Priority**: Medium`n`n> As any user, I want results paginated and sortable, so that large result sets are usable.`n`n- **AC1:** Multi-page pagination works smoothly.`n- **AC2:** Sorting by price ascending/descending works." },
  @{ title = "[B08] Compute today's avg/modal price per commodity"; body = "### Backlog Item B08`n**Area**: Summary | **Points**: 3 | **Priority**: High`n`n> As any user, I want to see today's average/modal price per commodity, so that I get a quick read without searching.`n`n- **AC1:** Computed modal price accurately aggregates records for today." },
  @{ title = "[B09] Compute % change vs previous reporting day"; body = "### Backlog Item B09`n**Area**: Summary | **Points**: 3 | **Priority**: High`n`n> As a farmer, I want to see how much a price moved since yesterday, so that I can judge whether to sell now.`n`n- **AC1:** Computes (today - yesterday) / yesterday * 100.`n- **AC2:** Shows 'no prior data' if yesterday's data is missing." },
  @{ title = "[B10] Dashboard indicator widgets"; body = "### Backlog Item B10`n**Area**: Summary | **Points**: 2 | **Priority**: Medium`n`n> As any user, I want indicators on the dashboard home, so that I see the market health at a glance.`n`n- **AC1:** Indicators for avg price, % change, and markets reporting render live." },
  @{ title = "[B11] Click-through from indicator to record list"; body = "### Backlog Item B11`n**Area**: Drill-down | **Points**: 2 | **Priority**: Medium`n`n> As any user, I want to click an indicator to see the underlying records, so that I can verify or investigate a number.`n`n- **AC1:** Drill-down shows underlying record set for that widget." },
  @{ title = "[B12] Single-record / full-history detail view"; body = "### Backlog Item B12`n**Area**: Drill-down | **Points**: 3 | **Priority**: Medium`n`n> As any user, I want a full history for one market+commodity, so that I can see the trend over time.`n`n- **AC1:** Shows chronological list of historical records." },
  @{ title = "[B13] Rule: flag price change beyond threshold"; body = "### Backlog Item B13`n**Area**: Alerts | **Points**: 3 | **Priority**: High`n`n> As a farmer, I want to be alerted when a price moves sharply, so that I don't miss a good/bad moment to sell.`n`n- **AC1:** Change beyond threshold (>15%) creates an alert.`n- **AC2:** Below threshold does not." },
  @{ title = "[B14] Rule: flag market missing today's data"; body = "### Backlog Item B14`n**Area**: Alerts | **Points**: 2 | **Priority**: Medium`n`n> As a market analyst, I want to know when a market hasn't reported today, so that gaps in coverage are visible.`n`n- **AC1:** Market with no record by cutoff appears in exceptions list." },
  @{ title = "[B15] Alerts/exceptions list view"; body = "### Backlog Item B15`n**Area**: Alerts | **Points**: 2 | **Priority**: Medium`n`n> As any user, I want a dedicated alerts view, so that exceptions aren't buried in the normal dashboard.`n`n- **AC1:** Dedicated view lists active alerts sorted newest-first." }
)

foreach ($item in $issues) {
  $json = @{ title = $item.title; body = $item.body } | ConvertTo-Json
  Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/issues" `
    -Method Post `
    -Headers @{ Authorization = "Bearer $token"; "Accept" = "application/vnd.github+json" } `
    -ContentType "application/json" `
    -Body $json
  Write-Host "Created issue: $($item.title)"
}
```
