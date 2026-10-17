# AMPT DevOps Final Presentation Slide Outline (12 Slides)

This presentation outline covers the complete DevOps lifecycle implementation for the **Agricultural Market Price Tracker (AMPT)** project.

---

## Slide 1: Title Slide & Project Introduction
- **Slide Title**: Automated End-to-End DevOps Lifecycle for Agricultural Market Price Tracker (AMPT)
- **Subtitle**: Enterprise CI/CD, Containerization, Automated Testing & Configuration Management
- **Presenter**: Aditi Gawari (Roll No: 23102A0025)
- **Course**: DevOps Semester 7 Capstone Project
- **Visuals**: Logos of Java, Spring Boot, Jenkins, Docker, Ansible, Selenium, and Git.
- **Speaker Notes**:
  > *"Good morning respected evaluators. Today I will present our complete end-to-end DevOps pipeline built for the Agricultural Market Price Tracker application. Over the course of 15 systematic tasks, we transitioned a monolithic Java enterprise app into an automated, containerized, and reliably provisioned system with continuous testing and deployment."*

---

## Slide 2: Problem Statement & Project Objectives
- **Problem Statement**:
  - Agricultural price monitoring requires high data integrity, rapid feature iteration, and zero-downtime reliability for farmers and mandi administrators.
  - Manual builds, ad-hoc server deployments, and untested releases lead to production downtime, configuration drift, and deployment bottlenecks.
- **Key Objectives**:
  - Establish automated GitFlow branching and multi-stage Jenkins CI pipelines.
  - Implement automated quality gates (unit tests & headless Chrome Selenium UI journeys).
  - Package lightweight, hardened, immutable Docker containers pushed to Docker Hub.
  - Automate infrastructure provisioning and zero-downtime atomic rollbacks with Ansible.
- **Visuals**: Comparison diagram: "Traditional Fragile Deployment" vs. "Modern Automated DevOps Pipeline".
- **Speaker Notes**:
  > *"Our primary objective was to replace error-prone manual operations with fully codified, automated pipelines. We designed a continuous delivery workflow where every code commit is built, tested against UI quality gates, packaged as an immutable image, and deployed with zero drift."*

---

## Slide 3: Application Architecture & Technology Stack
- **Application Overview**:
  - Agricultural Market Price Tracker (AMPT) — Spring Boot 3.3.4, Java 21, Spring Data JPA, Thymeleaf, and H2 database.
  - Provides real-time modal price tracking, percentage variance analysis, mandi filings, and automated market volatility alerts.
- **DevOps Toolchain**:
  - **SCM**: Git & GitHub (GitFlow, protected branches, pull requests).
  - **Build Automation**: Apache Maven 3.9 (Surefire & Failsafe).
  - **Continuous Integration**: Jenkins 2.x Declarative Pipeline.
  - **Automated Testing**: JUnit 5, Mockito, Selenium WebDriver, Headless Google Chrome.
  - **Containerization**: Docker, Docker Compose, Docker Hub (`aditiprakash21/ampt`).
  - **Configuration Management**: Ansible Core (Idempotent roles, Systemd units).
- **Visuals**: Technology badge stack and high-level 3-tier application architecture diagram.
- **Speaker Notes**:
  > *"The AMPT platform runs on Java 21 and Spring Boot. Our DevOps toolchain brings together industry-standard tools: Git for source control, Maven for build lifecycle management, Jenkins for pipeline orchestration, Selenium for UI acceptance gates, Docker for container packaging, and Ansible for infrastructure provisioning."*

---

## Slide 4: GitFlow Branching Strategy & Source Control
- **Branching Model**:
  - `main`: Production releases, protected branch, strictly tagged (`v1.0.0`, `v1.1.0`).
  - `develop`: Nightly/continuous integration branch where feature branches merge.
  - `feature/*`: Granular feature branches per task (e.g., `feature/task11-docker-lifecycle`, `feature/task14-reliability`).
- **Traceability & Governance**:
  - Semantic commit messages (`feat`, `fix`, `chore`, `docs`).
  - Pull Request reviews prior to merge.
  - Automated CI trigger upon push to `develop`.
- **Visuals**: GitFlow branching graph diagram illustrating feature merges and release tags.
- **Speaker Notes**:
  > *"We enforced strict source control governance using GitFlow. Developers never push directly to main or develop. Every feature was engineered on isolated branches, validated, and integrated via pull requests, ensuring full auditability and clean release tagging."*

---

## Slide 5: Continuous Integration Pipeline Architecture (Jenkins)
- **Declarative Pipeline Stages**:
  1. `Checkout`: Pulls latest commit from GitHub SCM.
  2. `Build`: Compiles Java 21 bytecode with `mvn clean package -DskipTests`.
  3. `Unit & UI Tests`: Executes 33 unit tests and 5 Selenium UI journeys.
  4. `Quality Gate Validation`: Checks JUnit XML reports before proceeding.
  5. `Package`: Archives versioned WAR artifacts with fingerprinting.
  6. `Docker Build & Push`: Builds multi-stage image, logs into Docker Hub, pushes tags.
  7. `Deploy & Smoke Test`: Runs container on port 8085 and verifies `/actuator/health`.
- **Visuals**: Jenkins Stage View screenshot showing all stages green with execution times.
- **Speaker Notes**:
  > *"Our Jenkins declarative pipeline orchestrates the entire build-to-deploy lifecycle. Notice the strict stage progression: unit tests and browser tests execute before packaging and container build stages. Any failure immediately stops the pipeline, ensuring broken code cannot be deployed."*

---

## Slide 6: Automated Quality Gates & Selenium Testing
- **Test Layers**:
  - **Unit & Slice Tests**: 33 tests verifying controllers, business services, alert thresholds, and repository queries (0 failures).
  - **End-to-End Acceptance Tests**: 5 headless Chrome browser journeys via Selenium WebDriver covering Login, Price Entry Form validation, Dashboard cards, Search drill-down, and Volatility Alerts.
- **Resilience Design**:
  - Dynamic ChromeDriver resolution via `WebDriverManager`.
  - Page Object Model (POM) pattern for maintainable locator isolation.
  - Automated screenshot capture on test failure using custom JUnit 5 test execution listeners.
- **Visuals**: Selenium test execution log showing all 5 journeys passing in 40.9 seconds.
- **Speaker Notes**:
  > *"Quality gates are the heart of reliable CI. In addition to our 33 unit tests, we implemented Selenium acceptance testing using the Page Object Model in headless Chrome. This verifies the complete user journey through actual DOM interaction, eliminating visual and functional regressions before deployment."*

---

## Slide 7: Docker Containerization & Image Hardening
- **Container Architecture**:
  - Multi-stage build based on `eclipse-temurin:21-jre-jammy`.
  - Non-root user `ampt` (UID 10001) for strict runtime process isolation.
  - Minimal image size (<280 MB) without development dependencies or build tools.
- **Registry & Delivery**:
  - Published to Docker Hub at `aditiprakash21/ampt`.
  - Dual tagging: build numbers (e.g., `:36`) and semantic release tags (`:1.1.0`, `:latest`).
  - Native Docker `HEALTHCHECK` configured against `/actuator/health`.
- **Visuals**: Diagram showing Docker layer caching and Docker Hub repository tags table.
- **Speaker Notes**:
  > *"For containerization, we prioritized security and efficiency. Our multi-stage Dockerfile drops root privileges, runs under a dedicated ampt user, and bakes in a native Docker HEALTHCHECK. We published versioned and latest images directly to Docker Hub."*

---

## Slide 8: Configuration Management with Ansible
- **Modular Role Architecture**:
  - `roles/common`: Installs baseline packages (`curl`, `openjdk-21-jre-headless`), configures system users and security privileges.
  - `roles/ampt_app`: Manages directory layouts, templates `application.yml` and `ampt.env`, deploys application archives, and registers systemd service units.
- **Dual Target Node Support**:
  - Native Systemd target nodes (WSL2 Ubuntu).
  - Container-based SSH target nodes (using non-systemd process runner fallback).
- **Visuals**: Tree diagram of the `ansible/` directory structure showing roles, inventory, and playbooks.
- **Speaker Notes**:
  > *"For host configuration management, we chose Ansible for its agentless architecture. We structured our configuration into reusable roles: common for base packages and ampt_app for application services. It supports both modern systemd Linux distributions and containerized target nodes."*

---

## Slide 9: Provisioning Reliability & Pure Idempotency
- **Idempotency Proof (`changed=0`)**:
  - First run on clean node: `ok=20, changed=14, failed=0` (Complete node initialization).
  - Second immediate run: `ok=18, changed=0, failed=0` (Zero unintended modifications).
- **Benefits**:
  - Eliminates configuration drift across heterogeneous environments.
  - Playbooks can be safely run at any time by automated schedulers without downtime.
  - Handlers prevent redundant service restarts when configurations remain unchanged.
- **Visuals**: Side-by-side terminal logs comparing clean provisioning run (`changed=14`) vs. idempotency run (`changed=0`).
- **Speaker Notes**:
  > *"Idempotency is the cornerstone of reliability. As demonstrated in our evidence logs, executing our playbook a second time on an already provisioned node reports changed=0. No files are unnecessarily touched, and the running application is never needlessly restarted."*

---

## Slide 10: Zero-Downtime Release & Automated Rollback
- **Release Strategy**:
  - Versioned release directory hierarchy: `/opt/ampt/releases/<version>/`.
  - Atomic symlink switching: `/opt/ampt/current` points to the active release.
- **Rollback Mechanism (`rollback.yml`)**:
  - Validates that the previous release directory exists on disk.
  - Atomically switches `/opt/ampt/current` back to the stable version (e.g., `v1.0.0`).
  - Restarts systemd daemon and monitors Spring Boot Actuator until status is `UP`.
  - Zero compilation, zero artifact re-download, rollback completed in seconds.
- **Visuals**: Diagram illustrating atomic symlink pointer redirecting from `v1.0.1` back to `v1.0.0`.
- **Speaker Notes**:
  > *"Our deployment strategy avoids in-place file replacement. Every release lives in its own versioned directory with an atomic symlink pointing to the current version. If an issue occurs, our rollback playbook redirects the symlink back to the previous stable release within seconds."*

---

## Slide 11: Key Engineering Challenges & Solutions
- **Real-World Issues Overcome**:
  1. *Windows Batch Pipe Whitespace*: Trailing space in `echo %TOKEN% | docker login` caused authentication rejections; resolved using direct parameter quoting and Jenkins credential bindings.
  2. *Host Disk Exhaustion*: Host storage exhaustion froze Docker Desktop; resolved by pruning build cache layers and cleaning temporary files.
  3. *Database Seed Ordering*: H2 foreign key constraint failures during early boot; resolved by ordering master vs. transactional tables in `data.sql` and deferring datasource initialization.
  4. *Dynamic Driver Mismatch*: Chrome 155 update broke pinned ChromeDriver 154; resolved by integrating `WebDriverManager` for runtime driver resolution.
- **Visuals**: Table mapping "Challenge Encountered" -> "Root Cause" -> "Engineered Solution".
- **Speaker Notes**:
  > *"Throughout this project, we solved several critical production-grade challenges: Windows CLI pipe whitespace errors, host storage exhaustion, database initialization constraints, and dynamic browser driver incompatibilities. Overcoming these strengthened the overall pipeline resilience."*

---

## Slide 12: Conclusion & Future Enhancements
- **Project Summary**:
  - Successfully achieved all 15 DevOps lifecycle milestones.
  - Established a fully automated, continuous delivery pipeline from Git commit to live production verification.
  - Tagged production release `v1.1.0` on GitHub.
- **Future Roadmap**:
  - Orchestration migration to Kubernetes (K8s / Helm Charts) with automated HPA (Horizontal Pod Autoscaling).
  - Blue/Green deployments using Nginx reverse proxy or cloud load balancers.
  - Distributed telemetry monitoring using Prometheus, Grafana, and OpenTelemetry.
- **Visuals**: Final production dashboard screenshot showing "Release v1.1.0 Verified Continuous Delivery Pipeline".
- **Speaker Notes**:
  > *"In conclusion, AMPT has evolved from raw source code into an enterprise-ready, automated continuous delivery ecosystem. In the future, we plan to extend this architecture to Kubernetes orchestration and Prometheus monitoring. Thank you for your time, and I welcome any questions."*
