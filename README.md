# Agricultural Market Price Tracker (AMPT)

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg?logo=apachemaven)](https://maven.apache.org/)
[![Git Flow](https://img.shields.io/badge/Branching-Git%20Flow-informational.svg?logo=git)](docs/BRANCHING.md)
[![DevOps Pipeline](https://img.shields.io/badge/Pipeline-Jenkins%20%7C%20Docker%20%7C%20Ansible%20%7C%20Selenium-purple.svg)](jenkins/)

Centralized Web Application and Automated DevOps Pipeline for Agricultural Market Price Tracking and Trend Analysis.

---

## 1. Project Overview

The **Agricultural Market Price Tracker (AMPT)** is an enterprise-grade web application engineered to solve market opacity, fragmented mandi reporting, and delayed price discovery for farmers, agricultural traders, and commodity analysts. 

In addition to serving real-time agricultural price data, AMPT serves as the primary artifact for a full-lifecycle **DevOps Engineering Pipeline**, demonstrating modern automated continuous integration, containerization, configuration management, automated UI gating, and container orchestration.

---

## 2. Technology Stack

| Layer | Technology | Version / Tooling | Rationale |
|---|---|---|---|
| **Core Platform** | Java / OpenJDK | 21 LTS | Modern language features, Virtual Threads support, long-term support. |
| **Framework** | Spring Boot | 3.3.4 | Rapid web application scaffold, production-ready metrics, Spring Data JPA. |
| **Presentation** | Thymeleaf + CSS3 | Modern Responsive UI | Server-rendered templates, fast rendering, zero heavy frontend node build dependencies. |
| **Persistence** | Spring Data JPA / Hibernate | 6.5+ | Object-relational mapping, declarative queries, schema generation. |
| **Database** | H2 Database / MySQL | H2 2.2 / MySQL 8.0 | Embedded file-based H2 for instant local development; MySQL container for staging/prod. |
| **Packaging** | Apache Maven | 3.9+ (`WAR`) | Dual packaging: runnable via embedded Tomcat (`8082`) or deployable to external Tomcat. |
| **VCS & Collaboration** | Git / GitHub | Git Flow | Robust feature-branch workflow, issue tracking, and automated CI pull requests. |
| **Continuous Integration**| Jenkins | Declarative Pipeline | Automated builds, test execution, SonarQube analysis, and artifact generation. |
| **Containerization** | Docker | Multi-stage Dockerfile | Containerized application and database microservices for consistency across stages. |
| **Configuration Mgmt** | Ansible | Playbooks & Roles | Automated server provisioning, Tomcat deployment, and environment configuration. |
| **Automated Testing** | JUnit 5 + Selenium | Selenium WebDriver 4.x | Automated unit testing and headless browser E2E quality gating before release. |

---

## 3. Key Features & Functional Scope

The application fulfills a frozen 15-item Agile Product Backlog (**B01–B15**):
- **Commodity & Market Data Model (B01)**: Relational schema managing Markets, Commodities, Price Records, and Users.
- **Price Entry & Validation (B03, B04)**: Web forms with server-side validation to record and edit daily arrivals, minimum, maximum, and modal prices.
- **Role-Based Access Control (B05)**: Restricts data entry and updates to authorized market personnel while keeping search public.
- **Search & Filter (B06, B07)**: Dynamic search across commodities, mandis, and date intervals with sortable, paginated result tables.
- **Daily Statistical Summaries (B08, B09, B10)**: Real-time calculation of modal prices, percentage change versus prior day, and market reporting health indicators.
- **Historical Drill-Down (B11, B12)**: Detailed chronological price trajectories for individual market-commodity pairs.
- **Automated Price Anomaly & Missing Data Alerts (B13, B14, B15)**: Configurable threshold rules detecting price spikes (>15%) and delinquent mandis missing daily filings.

---

## 4. How to Build

### Prerequisites
- **JDK 21 LTS** configured in `JAVA_HOME` and available in `PATH`.
- **Apache Maven 3.9+** configured in `PATH`.
- **Git** version control client.

### Compile, Test & Package
```bash
# Clean target, compile sources, run JUnit 5 tests, and generate WAR package
mvn clean package
```
- Test reports are generated in `target/surefire-reports/`.
- Packaged executable WAR is generated at `target/ampt.war`.

---

## 5. How to Run Locally

### Option A: Embedded Server (Default H2 Database)
```bash
mvn spring-boot:run
```
*Alternatively, run the packaged artifact directly:*
```bash
java -jar target/ampt.war
```

- **Application Port**: `8082` *(configured in `application.yml` to prevent conflicts with Jenkins on `8080` and external Tomcat on `8081`)*.
- **Database Location**: Local file-backed database at `./data/amptdb`.

### Option B: MySQL Profile (Docker / CI Staging)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql \
  -DDB_URL=jdbc:mysql://localhost:3306/amptdb \
  -DDB_USER=ampt \
  -DDB_PASSWORD=ampt
```

### Application Endpoints

| Resource | URL | Description |
|---|---|---|
| **AMPT Dashboard** | [http://localhost:8082/](http://localhost:8082/) | Main dashboard with summary widgets and seeded records |
| **Actuator Health** | [http://localhost:8082/actuator/health](http://localhost:8082/actuator/health) | Readiness and liveness endpoint for deployment verification |
| **Actuator Info** | [http://localhost:8082/actuator/info](http://localhost:8082/actuator/info) | Application build and metadata status |
| **H2 Web Console** | [http://localhost:8082/h2-console](http://localhost:8082/h2-console) | Database browser (JDBC: `jdbc:h2:file:./data/amptdb`, User: `sa`, Pwd: *(empty)*) |

---

## 6. Git Branching Strategy

The repository strictly follows the **Git Flow** branching model. For complete details, see [docs/BRANCHING.md](docs/BRANCHING.md).

```
main (Releases & Production)
  │
develop (Active Integration)
  ├── feature/B01-data-model
  ├── feature/B03-price-entry
  ├── bugfix/B04-update-timestamp
  └── release/v1.0.0
```

- **`main`**: Protected. Production releases only. Every merge is tagged (e.g., `v1.0.0`).
- **`develop`**: Protected. Continuous integration branch. All features merge here via Pull Request.
- **`feature/*`**: Branched from `develop`, merged back into `develop` via PR.
- **`bugfix/*`**: Non-critical fixes branched from and merged into `develop`.
- **`release/*`**: Stabilization branches preparing a new version release from `develop` into `main`.
- **`hotfix/*`**: Urgent production fixes branched directly from `main` and merged into both `main` and `develop`.

---

## 7. Project Structure

```text
Agricultural-Market-Price-Tracker/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md             # Standardized bug reporting template
│   │   ├── feature_request.md        # User story & acceptance criteria template
│   │   └── task.md                   # DevOps & engineering task template
│   └── pull_request_template.md      # PR checklist & review verification template
├── ansible/                          # Ansible playbooks & inventory (Task 14)
│   └── .gitkeep
├── docker/                           # Multi-stage Dockerfiles & Compose (Task 13)
│   └── .gitkeep
├── docs/                             # Architecture, SRS, and DevOps documentation
│   ├── 02-agile-planning-devops-workflow.md
│   ├── 03-architecture.md            # Software Requirements & Architectural Design
│   ├── BRANCHING.md                  # Detailed Git Flow & commit message guidelines
│   ├── issues-to-create.md           # 15 Agile backlog user stories (B01-B15)
│   ├── selenium-tests.md             # Automated UI testing strategy (Task 12)
│   ├── evidence/                     # Task execution verification evidence & logs
│   │   ├── task03/                   # Actuator dumps, dashboard responses, build logs
│   │   └── task04/                   # Git log, branch listings, issue trackers
│   └── images/                       # Architectural, ER, and Use-case diagrams
├── jenkins/                          # Jenkinsfile pipeline-as-code definitions (Task 10)
│   └── .gitkeep
├── src/
│   ├── main/
│   │   ├── java/com/ampt/            # Spring Boot application source code
│   │   │   ├── controller/           # Spring MVC Web Controllers
│   │   │   ├── entity/               # JPA Domain Entities (Market, Commodity, etc.)
│   │   │   ├── repository/           # Spring Data JPA Repositories
│   │   │   └── AmptApplication.java  # Main application entry point
│   │   └── resources/
│   │       ├── application.yml       # Application profiles & port configuration
│   │       ├── data.sql              # Seed data for agricultural markets & commodities
│   │       └── templates/            # Thymeleaf HTML views
│   └── test/java/com/ampt/           # JUnit 5 integration and unit test suite
├── .gitignore                        # Comprehensive ignore rules for Java, IDEs, Docker, Jenkins
├── pom.xml                           # Maven project object model & dependency config
└── README.md                         # Project documentation and engineering guide
```

---

## 8. DevOps Engineering Roadmap

| Task | Milestone / Deliverable | Status | Evidence / Docs |
|---|---|:---:|---|
| **Task 01** | Problem Definition & Scope Specification | Done | [docs/01-problem-definition-scope.md](docs/01-problem-definition-scope.md) |
| **Task 02** | Agile Planning & DevOps Workflow (15 Backlog Stories) | Done | [docs/02-agile-planning-devops-workflow.md](02-agile-planning-devops-workflow.md) |
| **Task 03** | Architecture, SRS, Entities & Health Endpoints | Done | [docs/03-architecture.md](docs/03-architecture.md) |
| **Task 04** | Git & GitHub Repo Init, Branching, Issues, Templates | Done | [docs/BRANCHING.md](docs/BRANCHING.md), [docs/issues-to-create.md](docs/issues-to-create.md) |
| **Task 05** | Feature 1 on Branch: Data Entry, Search & Role Check | Done | [docs/05-feature1.md](docs/05-feature1.md), [review-notes](docs/evidence/task05/review-notes.md) |
| **Task 06** | Unit Testing & Test Coverage (JUnit 5 & JaCoCo) | Upcoming | `src/test/` |
| **Task 07** | Static Code Analysis & Linting | Upcoming | SonarQube / Checkstyle |
| **Task 08** | Artifact Management & Versioning | Upcoming | Nexus / Artifactory |
| **Task 09** | CI Pipeline Setup | Upcoming | `jenkins/` |
| **Task 10** | Continuous Deployment to Staging | Upcoming | `jenkins/Jenkinsfile` |
| **Task 11** | Database Migration Automation | Upcoming | Flyway / Liquibase |
| **Task 12** | Automated UI & Acceptance Testing | Upcoming | [docs/selenium-tests.md](docs/selenium-tests.md) |
| **Task 13** | Containerization with Docker & Docker Compose | Upcoming | `docker/` |
| **Task 14** | Configuration Management & Provisioning with Ansible | Upcoming | `ansible/` |
| **Task 15** | Monitoring, Logging & DevOps Project Wrap-up | Upcoming | Actuator / Prometheus |

---

## 9. GitHub Issues & Backlog Mapping

The project's product backlog consists of 15 defined user stories (**B01–B15**). Complete specifications, acceptance criteria, and issue bodies are maintained in [docs/issues-to-create.md](docs/issues-to-create.md).

---

## 10. Contributors & License

- **Developer / Student**: Aditi Gawari (Roll No: 23102A0025, Semester 7 DevOps)
- **Repository**: [https://github.com/KabhiAditi/Agricultural-Market-Price-Tracker](https://github.com/KabhiAditi/Agricultural-Market-Price-Tracker)
- **License**: Academic & Educational Use — DevOps Engineering Curriculum.
<!-- Jenkins CI trigger test -->