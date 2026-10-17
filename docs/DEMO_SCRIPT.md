# AMPT DevOps Live Evaluation Demo Script (5-Minute Walkthrough)

This script provides an exact, minute-by-minute operational runbook to deliver a 5-minute live demonstration of the **Agricultural Market Price Tracker (AMPT)** DevOps lifecycle.

---

## Demo Schedule & Timeline

```
[00:00 - 01:00]  Minute 1: Architectural Overview & Source Control Flow
[01:00 - 02:00]  Minute 2: Continuous Integration & Selenium Quality Gate
[02:00 - 03:00]  Minute 3: Docker Containerization & Registry Push
[03:00 - 04:00]  Minute 4: Ansible Configuration Management & Idempotency
[04:00 - 05:00]  Minute 5: Live App Verification, Actuator Health & Rollback
```

---

## Minute 1: Architecture & Source Control Strategy (0:00 – 1:00)

### What to Show:
1. Open the project root in IDE or terminal.
2. Show Git branch structure: `git log --graph --oneline -n 6`.
3. Display GitHub repository: `https://github.com/KabhiAditi/Agricultural-Market-Price-Tracker`.

### What to Say:
> *"Good morning/afternoon. Today I am demonstrating the end-to-end DevOps pipeline for the Agricultural Market Price Tracker (AMPT), an enterprise Java 21 Spring Boot application. We adopted a strict GitFlow branching model: development takes place on feature branches, merges to `develop` via pull requests, and releases are tagged onto `main`. All 15 DevOps lifecycle tasks have been fully implemented, validated with logs, and pushed to GitHub."*

---

## Minute 2: CI Pipeline & Automated Quality Gate (1:00 – 2:00)

### What to Show:
1. Open Jenkins Dashboard (`http://localhost:8080/job/ampt-pipeline/`).
2. Show the multi-stage declarative pipeline visual view.
3. Highlight test reporting and JUnit test results:
   - 33 Unit & Service Tests: **0 Failures**.
   - 5 Headless Chrome Selenium Acceptance Tests: **0 Failures**.

### What to Say:
> *"Our Jenkins CI pipeline enforces strict quality gates at the code and functional level. On code change, Jenkins triggers Maven compilation, executes our 33 Spring Boot unit and slice tests, and boots an embedded test container to execute 5 automated end-to-end user journeys using headless Google Chrome via Selenium. If any UI assertion or unit test fails, the build breaks immediately, preventing broken artifacts from ever reaching production."*

---

## Minute 3: Containerization & Registry Push (2:00 – 3:00)

### What to Show:
1. Show the production multi-stage [`Dockerfile`](../Dockerfile).
2. Point out security best practices: non-root `ampt` user, `eclipse-temurin:21-jre-jammy` minimal runtime image, baked-in `HEALTHCHECK`.
3. Open Docker Hub repository: `https://hub.docker.com/r/aditiprakash21/ampt/tags`.
4. Point out the versioned tags (`1.1.0`, `36`, `latest`).

### What to Say:
> *"Once tests pass, our pipeline builds an immutable Docker container based on Temurin JRE 21. Notice that we enforce the principle of least privilege: the container runs under a dedicated, non-root user `ampt` (UID 10001), exposes port 8080, and features an integrated Docker health check. Jenkins authenticates with Docker Hub and pushes both unique build numbers and semantic release tags."*

---

## Minute 4: Ansible Idempotency & Clean Provisioning (3:00 – 4:00)

### What to Show:
1. Open terminal and run idempotency check or show the logged recap:
   ```bash
   ansible-playbook -i ansible/inventory.ini ansible/site.yml -e app_version=v1.1.0 --limit wsl-node
   ```
2. Highlight the Play Recap output:
   `wsl-node : ok=18 changed=0 unreachable=0 failed=0 skipped=1`
3. Point out role structure: `roles/common` and `roles/ampt_app`.

### What to Say:
> *"For production host configuration management, we implemented an Ansible architecture consisting of reusable `common` and `ampt_app` roles. Our playbook is 100% idempotent. When executed on an already converged node, as shown in the recap, it reports `changed=0`. System users, directories, configs, and services are preserved without configuration drift or unnecessary service restarts."*

---

## Minute 5: Live App Verification & Rollback Demo (4:00 – 5:00)

### What to Show:
1. Open browser to live application: `http://127.0.0.1:8080/`.
2. Show the live dashboard:
   - Commodity price cards (Tomato, Onion, Wheat).
   - Volatility alert banner.
   - Footer showing **Release v1.1.0 | Verified Continuous Delivery Pipeline**.
3. Open Actuator Health endpoint: `http://127.0.0.1:8080/actuator/health`:
   - JSON response showing `"status": "UP"`, H2 database UP, diskSpace UP.
4. Show the rollback playbook command:
   ```bash
   ansible-playbook -i ansible/inventory.ini ansible/rollback.yml -e previous_version=v1.0.0 --limit wsl-node
   ```

### What to Say:
> *"Here is our live running production application on port 8080. You can see the live market prices, price variance indicators, and the release footer verifying our newly deployed `v1.1.0`. The Spring Boot Actuator health endpoint returns HTTP 200 with all subsystem metrics reporting UP. Finally, should any production anomaly ever occur, our automated zero-downtime rollback playbook `rollback.yml` instantly switches the release symlink back to `v1.0.0` within seconds without recompilation. Thank you, and I am now ready for your questions."*
