# Git Branching Strategy & Workflow Guide

## 1. Overview
The **Agricultural Market Price Tracker (AMPT)** project adheres to a structured **Git Flow** branching model designed for seamless collaboration, automated CI/CD gating, and clean release management.

---

## 2. Core Branches

| Branch | Classification | Source | Merges Into | Purpose & Rules |
|--------|----------------|--------|-------------|-----------------|
| `main` | Production / Release | None | None | Protected branch. Contains stable, production-ready code. Direct commits are strictly prohibited. Releases are tagged (`vX.Y.Z`). |
| `develop` | Integration Baseline | `main` | `main` | Protected branch. Default branch for ongoing team collaboration and CI pipeline integration. |

---

## 3. Supporting Branch Naming Conventions

All supporting branches must adhere to the naming taxonomy: `<type>/<issue-id>-<short-description>`.

```
                    ┌─► feature/B01-data-model ────┐
                    │                              ▼
main ───────────────┼────────────────────────────── develop ──► release/v1.0.0 ──► main (v1.0.0)
  ▲                 │                              ▲                  │
  └─ hotfix/fix-db ─┴──────────────────────────────┘                  └─► develop
```

### Branch Types

1. **Feature Branches (`feature/*`)**
   - **Format**: `feature/<issue-id>-<short-description>`
   - **Examples**: `feature/B01-data-model`, `feature/B03-price-entry-form`, `feature/B06-search-api`
   - **Created From**: `develop`
   - **Merged Into**: `develop`
   - **Purpose**: Implementing new user stories or system enhancements.

2. **Bugfix Branches (`bugfix/*`)**
   - **Format**: `bugfix/<issue-id>-<short-description>`
   - **Examples**: `bugfix/B04-update-timestamp-fix`, `bugfix/ui-table-alignment`
   - **Created From**: `develop`
   - **Merged Into**: `develop`
   - **Purpose**: Resolving non-critical bugs discovered during sprint development or testing.

3. **Release Branches (`release/*`)**
   - **Format**: `release/v<major>.<minor>.<patch>`
   - **Examples**: `release/v1.0.0`, `release/v1.1.0`
   - **Created From**: `develop`
   - **Merged Into**: `main` and `develop`
   - **Purpose**: Finalizing release polish, metadata updates, and release testing. Merging into `main` triggers a release tag.

4. **Hotfix Branches (`hotfix/*`)**
   - **Format**: `hotfix/<short-description>`
   - **Examples**: `hotfix/db-connection-pool-leak`, `hotfix/security-filter-bypass`
   - **Created From**: `main`
   - **Merged Into**: `main` AND `develop`
   - **Purpose**: Urgent production bug fixes requiring immediate rollout without waiting for the next sprint release.

---

## 4. End-to-End Developer Workflow

1. **Sync Local Workspace**:
   ```bash
   git checkout develop
   git pull origin develop
   ```

2. **Create Feature Branch**:
   ```bash
   git checkout -b feature/B06-search-api
   ```

3. **Develop & Commit (Conventional Commits)**:
   Make small, focused commits adhering to conventional commit specifications:
   - `feat: implement search API with commodity and market query parameters`
   - `test: add unit and repository integration tests for search endpoint`
   - `docs: update API documentation for /prices search filters`

4. **Run Local Validation**:
   Ensure compilation, tests, and formatting pass:
   ```bash
   mvn clean package
   ```

5. **Push Branch & Open Pull Request**:
   ```bash
   git push -u origin feature/B06-search-api
   ```
   Open a PR against `develop` using [.github/pull_request_template.md](file:///.github/pull_request_template.md).

6. **Review, CI Gating & Merge**:
   - Automated Jenkins / GitHub Actions CI builds and runs all unit & Selenium tests.
   - Code review approval required.
   - Merge via "Squash and merge" or "Rebase and merge" into `develop`.
   - Delete the remote feature branch post-merge.

---

## 5. Commit Message Standard
All commit messages must follow the [Conventional Commits](https://www.conventionalcommits.org/) convention:
```
<type>(<optional scope>): <short description in imperative mood>

[optional body explaining rationale]

[optional footer, e.g., Closes #B06]
```

### Recognized Types:
- `feat`: A new user-facing feature or API capability
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Formatting, missing semi-colons, no code changes
- `refactor`: Refactoring production code without altering behavior
- `test`: Adding or correcting unit, integration, or Selenium tests
- `chore`: Maintenance tasks, dependencies, build configs (`pom.xml`, `.gitignore`)
- `ci`: CI/CD pipeline, Jenkinsfile, Docker, Ansible configuration
