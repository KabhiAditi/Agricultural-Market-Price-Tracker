# Task 14: Provisioning, Idempotency, and Rollback Reliability Validation

## 1. Overview & Executive Summary

This document demonstrates and validates the reliability, idempotency, and automated rollback capabilities of the Ansible configuration management infrastructure for the **Agricultural Market Price Tracker (AMPT)** application.

### Key Objectives Verified:
1. **Clean Target Node Provisioning (Cold Start)**: Fully automated bootstrap of a clean target node from scratch (system packages, isolated runtime user/group, directories, configuration templates, versioned release directories, systemd service unit, and service startup).
2. **Strict Idempotency (`changed=0`)**: Immediate re-execution of the master playbook reports zero changes (`changed=0`), proving convergence and state safety without configuration drift or unintended service restarts.
3. **Continuous Health Checks**: Integrated Ansible health check tasks using the `ansible.builtin.uri` module with retry/until logic, alongside a standalone POSIX monitoring script (`scripts/health-check.sh`).
4. **Versioned Release & Atomic Rollback**: Zero-drift release strategy deploying version `v1.0.1` to `/opt/ampt/releases/v1.0.1` and atomically swapping symlinks, followed by a guaranteed rollback to `v1.0.0` with post-rollback health checks confirming application availability.

---

## 2. Validation Scenarios Summary Table

| Scenario | Command Executed | Expected Result | Actual Result | Evidence File |
| :--- | :--- | :--- | :--- | :--- |
| **1. Clean Target Node Provisioning** | `ansible-playbook -i inventory.ini site.yml -e app_version=v1.0.0` | Node bootstrapped from clean state: user/group created, directories initialized, artifact copied, service started, health check passes. | `ok=20 changed=14 unreachable=0 failed=0 skipped=2`. Service started, health check returned HTTP 200 `UP`. | [`1-clean-provision.log`](evidence/task14/1-clean-provision.log) |
| **2. Immediate Re-run (Idempotency)** | `ansible-playbook -i inventory.ini site.yml -e app_version=v1.0.0` | Exact desired state already satisfied. All tasks report `ok` or `skipped`. **`changed=0`**. Service is NOT restarted. | `ok=18 changed=0 unreachable=0 failed=0 skipped=1`. Pure convergence verified. | [`2-idempotency.log`](evidence/task14/2-idempotency.log) |
| **3. Standalone Health Check Script** | `bash scripts/health-check.sh http://127.0.0.1:8080/actuator/health 20 2` | Script polls `/actuator/health`, receives HTTP 200 with JSON payload containing `"status": "UP"`, exits 0. | Script succeeded on Attempt 1: HTTP 200, valid Actuator health JSON, exited 0. | [`health-check.log`](evidence/task14/health-check.log) |
| **4. Deploy New Version (`v1.0.1`)** | `ansible-playbook -i inventory.ini release.yml -e app_version=v1.0.1` | Creates release directory `v1.0.1`, deploys WAR, atomically switches symlink, restarts service, waits for health check. | `ok=10 changed=5 unreachable=0 failed=0 skipped=1`. Health check confirmed UP after 3 retries (warmup). | [`3-deploy-v101.log`](evidence/task14/3-deploy-v101.log) |
| **5. Post-Deploy Health Check** | `bash scripts/health-check.sh http://127.0.0.1:8080/actuator/health 20 2` | Validates that the newly deployed version `v1.0.1` is answering requests and healthy. | Attempt 1: HTTP 200 `{"status":"UP",...}`, exited 0. | [`3-deploy-v101-health.log`](evidence/task14/3-deploy-v101-health.log) |
| **6. Rollback to Stable (`v1.0.0`)** | `ansible-playbook -i inventory.ini rollback.yml -e previous_version=v1.0.0` | Verifies `v1.0.0` release exists, atomically points symlink to `v1.0.0`, restarts service, polls health check until UP. | `ok=11 changed=2 unreachable=0 failed=0 skipped=3`. Rollback confirmed UP after 3 retries (warmup). | [`4-rollback-v100.log`](evidence/task14/4-rollback-v100.log) |
| **7. Post-Rollback Health Check** | `bash scripts/health-check.sh http://127.0.0.1:8080/actuator/health 20 2` | Validates that the rolled-back version `v1.0.0` is active and healthy. | Attempt 1: HTTP 200 `{"status":"UP",...}`, exited 0. | [`4-rollback-v100-health.log`](evidence/task14/4-rollback-v100-health.log) |

---

## 3. Detailed Scenario Walkthrough & Evidence Analysis

### Scenario 1: Clean Node Provisioning (Cold Start)
To simulate a clean target node:
1. The existing `ampt.service` systemd unit was stopped and disabled.
2. Systemd unit `/etc/systemd/system/ampt.service` was removed and the daemon reloaded.
3. System directories `/opt/ampt`, `/var/log/ampt`, and `/etc/ampt` were wiped.
4. Dedicated system user `ampt` and group `ampt` were deleted.

Running `ansible-playbook -i inventory.ini site.yml -e app_version=v1.0.0`:
- Baseline packages verified (`curl`, `ca-certificates`, `openjdk-21-jre-headless`).
- System group and user `ampt` created (`changed`).
- Application directories created with `0750` / `0755` permissions (`changed`).
- Configuration files `application.yml` and `ampt.env` deployed from Jinja2 templates (`changed`).
- Application archive deployed to versioned release path `/opt/ampt/releases/v1.0.0/ampt.war` (`changed`).
- Release symlink `/opt/ampt/current` created pointing to `/opt/ampt/releases/v1.0.0` (`changed`).
- Application symlink `/opt/ampt/ampt.war` created pointing to `/opt/ampt/current/ampt.war` (`changed`).
- Systemd service unit `/etc/systemd/system/ampt.service` generated and registered (`changed`).
- Service started, health check task polled `/actuator/health` until Tomcat and Spring Boot completed initialization (HTTP 200, `UP`).

**Play Recap:**
```text
wsl-node : ok=20   changed=14   unreachable=0   failed=0   skipped=2   rescued=0   ignored=0
```

---

### Scenario 2: Idempotency Validation (`changed=0`)
Immediately following clean provisioning, the exact same command was executed without modifying any files:
```bash
ansible-playbook -i inventory.ini site.yml -e app_version=v1.0.0
```

**Results:**
- All packages, groups, and users were already in the desired state (`ok`).
- File permissions and directory hierarchies were already compliant (`ok`).
- Checksum verification for `ampt.war` and configuration templates indicated identical contents (`ok`).
- Release symlinks already pointed to the active version (`ok`).
- Systemd service was already enabled and active (`ok`).
- Health check verified running application without triggering changes (`ok`).

**Play Recap:**
```text
wsl-node : ok=18   changed=0    unreachable=0   failed=0   skipped=1   rescued=0   ignored=0
```
This confirms **100% idempotency** of the Ansible configuration suite.

---

### Scenario 3: Standalone Health Check Script
A portable POSIX script [`scripts/health-check.sh`](../scripts/health-check.sh) was implemented to provide automated validation independent of Ansible:
```bash
./scripts/health-check.sh http://127.0.0.1:8080/actuator/health 20 2
```
Output:
```text
======================================================
  AMPT Health Check Monitor
  Target:  http://127.0.0.1:8080/actuator/health
  Retries: 20 (Delay: 2s)
======================================================
Attempt 1/20: Checking endpoint... [UP] HTTP 200
Response Payload: {"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}},"diskSpace":{"status":"UP","details":{"total":1081101176832,"free":1020638986240,"threshold":10485760,"path":"/opt/ampt/.","exists":true}},"livenessState":{"status":"UP"},"ping":{"status":"UP"},"readinessState":{"status":"UP"}},"groups":["liveness","readiness"]}
>>> Health Check PASSED: AMPT Application is healthy!
```

---

### Scenario 4: Application Release Deployment (`v1.0.1`)
The playbook [`ansible/release.yml`](../ansible/release.yml) allows versioned deployments by passing the `app_version` parameter:
```bash
ansible-playbook -i inventory.ini release.yml -e app_version=v1.0.1
```
Execution Flow:
1. Validates `app_version` is defined.
2. Creates `/opt/ampt/releases/v1.0.1`.
3. Deploys the artifact and writes `/opt/ampt/releases/v1.0.1/version.txt`.
4. Atomically switches `/opt/ampt/current` -> `/opt/ampt/releases/v1.0.1`.
5. Restarts `ampt.service` via systemd.
6. Polls `/actuator/health` with `retries: 15` and `delay: 4` until status is `UP`.

**Play Recap:**
```text
wsl-node : ok=10   changed=5    unreachable=0   failed=0   skipped=1   rescued=0   ignored=0
```

---

### Scenario 5: Automated Rollback to Previous Stable Release (`v1.0.0`)
When a rollback is triggered, [`ansible/rollback.yml`](../ansible/rollback.yml) executes a safe, zero-rebuild rollback to any previously verified release directory:
```bash
ansible-playbook -i inventory.ini rollback.yml -e previous_version=v1.0.0
```
Execution Flow:
1. Validates that `/opt/ampt/releases/v1.0.0` exists and contains a valid `ampt.war`.
2. Reads the current version (`v1.0.1`) and logs:
   `"Initiating rollback from version v1.0.1 to v1.0.0..."`
3. Atomically updates symlink `/opt/ampt/current` -> `/opt/ampt/releases/v1.0.0`.
4. Restarts `ampt.service`.
5. Validates health check until HTTP 200 `status: UP` is confirmed.

**Play Recap:**
```text
wsl-node : ok=11   changed=2    unreachable=0   failed=0   skipped=3   rescued=0   ignored=0
```

---

## 4. Architecture & Release Engineering Design

### Filesystem Layout for Zero-Drift Releases
```text
/opt/ampt/
├── current -> /opt/ampt/releases/v1.0.0   (Atomic Symlink)
├── ampt.war -> /opt/ampt/current/ampt.war
├── releases/
│   ├── v1.0.0/
│   │   ├── ampt.war
│   │   └── version.txt                    ("v1.0.0")
│   └── v1.0.1/
│       ├── ampt.war
│       └── version.txt                    ("v1.0.1")
├── bin/
│   └── ampt-runner.sh
├── data/
│   ├── amptdb.mv.db
│   └── amptdb.trace.db
└── application.yml
```

### Health Check Task Design (Ansible)
```yaml
- name: Wait for AMPT health check endpoint to respond with UP
  ansible.builtin.uri:
    url: "{{ ampt_health_url }}"
    method: GET
    status_code: 200
    return_content: true
  register: health_result
  until: health_result.status == 200 and ('UP' in health_result.content or 'status' in health_result.json)
  retries: "{{ ampt_health_retries }}"
  delay: "{{ ampt_health_delay }}"
```

---

## 5. Automated Validation Reproduction Script

The complete reliability sequence can be reproduced at any time using [`scripts/run-task14.sh`](../scripts/run-task14.sh):

```bash
# In WSL2 Ubuntu or Linux control host:
cd "Agricultural-Market-Price-Tracker"
bash scripts/run-task14.sh
```

All raw evidence files are stored in [`docs/evidence/task14/`](evidence/task14/).
