<#
.SYNOPSIS
  Automates the creation of 15 Agile Backlog Issues (B01-B15) on GitHub.
.DESCRIPTION
  Requires either $env:GITHUB_TOKEN to be set, or prompts interactively.
  Targets https://github.com/KabhiAditi/Agricultural-Market-Price-Tracker
#>

param(
    [string]$Token = $env:GITHUB_TOKEN,
    [string]$Repo = "KabhiAditi/Agricultural-Market-Price-Tracker"
)

if (-not $Token) {
    $Token = Read-Host "Enter your GitHub Personal Access Token (classic with 'repo' scope or fine-grained with 'Issues: Write')"
}

if (-not $Token) {
    Write-Error "A GitHub token is required to create issues."
    exit 1
}

$issues = @(
  @{
    title = "[B01] Define data model (Market, Commodity, PriceRecord, AppUser/Role)"
    labels = @("enhancement", "foundation", "priority:high")
    body = "### Backlog Item B01`n**Area**: Foundation | **Points**: 3 | **Priority**: High`n`n> As a developer, I want a clear data model for markets, commodities, price records and users, so that every later feature has a stable schema to build on.`n`n- **AC1:** Market, Commodity, PriceRecord, AppUser tables exist with FKs linking PriceRecord to Market and Commodity.`n- **AC2:** PriceRecord requires date, min_price, max_price, modal_price, and arrival_qty."
  },
  @{
    title = "[B02] Project skeleton, build tool config, local DB schema/migration"
    labels = @("task", "foundation", "priority:high")
    body = "### Backlog Item B02`n**Area**: Foundation | **Points**: 3 | **Priority**: High`n`n> As a developer, I want a runnable local skeleton, so that I can start building features immediately.`n`n- **AC1:** 'mvn clean package' builds and starts without manual intervention.`n- **AC2:** Maven build config, folder structure, DB connection config present."
  },
  @{
    title = "[B03] Price-entry form (create) with validation"
    labels = @("enhancement", "data-entry", "priority:high")
    body = "### Backlog Item B03`n**Area**: Entry | **Points**: 3 | **Priority**: High`n`n> As a mandi official, I want to enter a new price record, so that today's price is available to everyone searching the app.`n`n- **AC1:** Valid submission creates PriceRecord visible in search.`n- **AC2:** Missing required field rejected with validation errors."
  },
  @{
    title = "[B04] Edit/correct existing price record (update)"
    labels = @("enhancement", "data-entry", "priority:high")
    body = "### Backlog Item B04`n**Area**: Entry | **Points**: 2 | **Priority**: High`n`n> As a mandi official, I want to correct a record I entered, so that mistakes don't stay live.`n`n- **AC1:** Valid edit updates in place and changes updated timestamp.`n- **AC2:** Invalid values rejected."
  },
  @{
    title = "[B05] Role check — only authorized roles enter/edit"
    labels = @("security", "data-entry", "priority:medium")
    body = "### Backlog Item B05`n**Area**: Entry | **Points**: 2 | **Priority**: Medium`n`n> As a system, I want only authorized roles to create/edit price data, so that data integrity is protected.`n`n- **AC1:** Unauthenticated/read-only user rejected (403).`n- **AC2:** Authorized user succeeds."
  },
  @{
    title = "[B06] Search API + UI (market, commodity, date range)"
    labels = @("enhancement", "dashboard", "priority:high")
    body = "### Backlog Item B06`n**Area**: Dashboard | **Points**: 4 | **Priority**: High`n`n> As any user, I want to search prices by market, commodity, and date range, so that I can find the exact data I need.`n`n- **AC1:** Search by commodity across multiple markets returns all matches.`n- **AC2:** Date range with no data returns clean empty-state."
  },
  @{
    title = "[B07] Paginated, sortable results list"
    labels = @("enhancement", "dashboard", "priority:medium")
    body = "### Backlog Item B07`n**Area**: Dashboard | **Points**: 2 | **Priority**: Medium`n`n> As any user, I want results paginated and sortable, so that large result sets are usable.`n`n- **AC1:** Multi-page pagination works smoothly.`n- **AC2:** Sorting by price ascending/descending works."
  },
  @{
    title = "[B08] Compute today's avg/modal price per commodity"
    labels = @("enhancement", "analytics", "priority:high")
    body = "### Backlog Item B08`n**Area**: Summary | **Points**: 3 | **Priority**: High`n`n> As any user, I want to see today's average/modal price per commodity, so that I get a quick read without searching.`n`n- **AC1:** Computed modal price accurately aggregates records for today."
  },
  @{
    title = "[B09] Compute % change vs previous reporting day"
    labels = @("enhancement", "analytics", "priority:high")
    body = "### Backlog Item B09`n**Area**: Summary | **Points**: 3 | **Priority**: High`n`n> As a farmer, I want to see how much a price moved since yesterday, so that I can judge whether to sell now.`n`n- **AC1:** Computes (today - yesterday) / yesterday * 100.`n- **AC2:** Shows 'no prior data' if yesterday's data is missing."
  },
  @{
    title = "[B10] Dashboard indicator widgets"
    labels = @("enhancement", "ui", "priority:medium")
    body = "### Backlog Item B10`n**Area**: Summary | **Points**: 2 | **Priority**: Medium`n`n> As any user, I want indicators on the dashboard home, so that I see the market health at a glance.`n`n- **AC1:** Indicators for avg price, % change, and markets reporting render live."
  },
  @{
    title = "[B11] Click-through from indicator to record list"
    labels = @("enhancement", "ui", "priority:medium")
    body = "### Backlog Item B11`n**Area**: Drill-down | **Points**: 2 | **Priority**: Medium`n`n> As any user, I want to click an indicator to see the underlying records, so that I can verify or investigate a number.`n`n- **AC1:** Drill-down shows underlying record set for that widget."
  },
  @{
    title = "[B12] Single-record / full-history detail view"
    labels = @("enhancement", "ui", "priority:medium")
    body = "### Backlog Item B12`n**Area**: Drill-down | **Points**: 3 | **Priority**: Medium`n`n> As any user, I want a full history for one market+commodity, so that I can see the trend over time.`n`n- **AC1:** Shows chronological list of historical records."
  },
  @{
    title = "[B13] Rule: flag price change beyond threshold"
    labels = @("enhancement", "alerts", "priority:high")
    body = "### Backlog Item B13`n**Area**: Alerts | **Points**: 3 | **Priority**: High`n`n> As a farmer, I want to be alerted when a price moves sharply, so that I don't miss a good/bad moment to sell.`n`n- **AC1:** Change beyond threshold (>15%) creates an alert.`n- **AC2:** Below threshold does not."
  },
  @{
    title = "[B14] Rule: flag market missing today's data"
    labels = @("enhancement", "alerts", "priority:medium")
    body = "### Backlog Item B14`n**Area**: Alerts | **Points**: 2 | **Priority**: Medium`n`n> As a market analyst, I want to know when a market hasn't reported today, so that gaps in coverage are visible.`n`n- **AC1:** Market with no record by cutoff appears in exceptions list."
  },
  @{
    title = "[B15] Alerts/exceptions list view"
    labels = @("enhancement", "alerts", "priority:medium")
    body = "### Backlog Item B15`n**Area**: Alerts | **Points**: 2 | **Priority**: Medium`n`n> As any user, I want a dedicated alerts view, so that exceptions aren't buried in the normal dashboard.`n`n- **AC1:** Dedicated view lists active alerts sorted newest-first."
  }
)

Write-Host "Creating 15 GitHub issues on repository: $Repo ..." -ForegroundColor Cyan

foreach ($item in $issues) {
    $payload = @{
        title = $item.title
        body  = $item.body
        labels = $item.labels
    } | ConvertTo-Json -Depth 5

    try {
        $response = Invoke-RestMethod -Uri "https://api.github.com/repos/$Repo/issues" `
            -Method Post `
            -Headers @{
                Authorization = "Bearer $Token"
                "Accept" = "application/vnd.github+json"
                "User-Agent" = "AMPT-DevOps-Script"
            } `
            -ContentType "application/json; charset=utf-8" `
            -Body $payload
        Write-Host " Created issue #$($response.number): $($item.title)" -ForegroundColor Green
    } catch {
        Write-Warning "Failed to create issue '$($item.title)': $_"
    }
}

Write-Host "Done! Visit: https://github.com/$Repo/issues" -ForegroundColor Cyan
