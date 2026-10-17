# DevOps Project Troubleshooting Guide: Real Issues & Solutions

This document chronicles every real issue, root cause analysis (RCA), and concrete engineering fix encountered throughout the end-to-end development, containerization, and automation of the **Agricultural Market Price Tracker (AMPT)** system.

---

## 1. Database Initialization & Foreign Key Ordering (`data.sql`)

### Symptom:
Spring Boot application failed to start during early boot phase with `org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException` referencing foreign key constraints.

### Root Cause:
In Spring Boot 3.x, `schema.sql` creates tables followed immediately by `data.sql` inserting initial seed data. The original `data.sql` script inserted `price_record` entries before the referenced `mandi` and `commodity` rows were created, causing H2 to throw referential integrity violations upon table population.

### Solution:
Restructured `data.sql` into a strict dependency-ordered hierarchy:
1. Master reference tables first (`roles`, `users`, `user_roles`).
2. Geographical and agricultural master tables (`commodities`, `mandis`).
3. Transactional and dependent tables last (`price_records`, `market_alerts`).
Configured `spring.jpa.defer-datasource-initialization=true` in `application.yml` so Hibernate validates schemas before SQL scripts execute.

---

## 2. Windows Batch CLI Pipe Space Bug in Jenkins (`docker login`)

### Symptom:
Jenkins CI/CD pipeline failed during the `Docker Push` stage with:
`"unauthorized: incorrect username or password"`
even though credentials in Jenkins credentials store and Docker Hub were verified correct.

### Root Cause:
In Windows batch scripts (`bat` step in Jenkinsfile), executing:
```bat
echo %DH_TOKEN% | docker login -u %DH_USER% --password-stdin
```
leaves a trailing whitespace before the pipe ` | `, which `cmd.exe` passes as part of the password string to `docker login`. Consequently, Docker Hub received `password ` rather than `password`.

### Solution:
1. Enclosed the credentials within quotes and eliminated the piping syntax:
```bat
docker login -u %DH_USER% -p "%DH_TOKEN%"
```
2. Used the Jenkins credentials binding `usernamePassword(credentialsId: "${params.DOCKERHUB_CREDS_ID}", usernameVariable: 'DH_USER', passwordVariable: 'DH_TOKEN')` to inject sanitized variables directly without shell expansion anomalies.

---

## 3. Host Disk Exhaustion Crisis on Windows (`0 bytes free`)

### Symptom:
Maven builds failed with `java.io.IOException: No space left on device` and Docker Desktop daemon hung indefinitely on all CLI invocations (`docker ps`, `docker version`).

### Root Cause:
Windows system drive (`C:`) reached 100% capacity with 0 MB remaining due to accumulated Docker build cache layers, Windows temp directories, and legacy ISO images in user storage. Docker Desktop enforces a minimum 2 GB safety threshold below which it pauses the virtual engine socket.

### Solution:
1. Flushed Windows Recycle Bin via `Clear-RecycleBin -Force`.
2. Cleaned AppData temporary storage and Windows CrashDumps.
3. Executed `docker builder prune -a` and removed dangling container images to recover over 3.5 GB of free drive space.
4. Added `.dockerignore` to avoid transferring unnecessary local build artifacts and directories into Docker build contexts.

---

## 4. Selenium WebDriver / Chrome Browser Version Mismatch

### Symptom:
UI acceptance tests failed to initialize the browser with `SessionNotCreatedException: This version of ChromeDriver only supports Chrome version X`.

### Root Cause:
The local Google Chrome browser auto-updated to version 155, while the cached local ChromeDriver binary was pinned to version 154, leading to binary incompatibility.

### Solution:
1. Implemented BoniGarcia's `WebDriverManager` in `AmptJourneysIT` to dynamically resolve, download, and bind the exact ChromeDriver matching the installed Google Chrome version at runtime.
2. Configured fallback system properties `-Dwebdriver.chrome.driver` and `-Dchrome.bin` in Maven pom profiles and Jenkinsfile.
3. Added headless arguments (`--headless=new`, `--no-sandbox`, `--disable-dev-shm-usage`, `--remote-allow-origins=*`) to ensure tests run reliably in both interactive desktop and headless CI environments.

---

## 5. Spring Security Filter Chain & Actuator Health Endpoint Block

### Symptom:
Jenkins Smoke Test and health check scripts received HTTP 401 Unauthorized or HTTP 403 Forbidden when calling `/actuator/health`.

### Root Cause:
Spring Security's default security configuration intercepted all HTTP endpoints and required basic authentication, including management actuator endpoints.

### Solution:
Configured `SecurityBeansConfig.java` to explicitly permit unauthenticated public access to health and metrics endpoints:
```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/health/**", "/actuator/info", "/css/**", "/js/**", "/login").permitAll()
    .anyRequest().authenticated()
);
```
Ensured `management.endpoints.web.exposure.include=health,info,metrics` in `application.yml`.

---

## 6. Windows CRLF vs Linux LF Line-Ending Corruption in Shell Scripts

### Symptom:
Bash scripts executed in WSL2 or Linux containers failed with syntax errors:
`scripts/health-check.sh: line 10: $'\r': command not found`
`set: pipefail: invalid option name`

### Root Cause:
When Git commits or checkouts occur on Windows hosts without strict line-ending rules, Git converts LF newlines to Windows CRLF (`\r\n`). Linux shells interpret `\r` as a literal carriage return character, corrupting shell built-ins.

### Solution:
1. Normalized script files using `sed -i 's/\r$//' scripts/*.sh`.
2. Created a repository-wide [`.gitattributes`](../.gitattributes) configuration:
```gitattributes
*.sh text eol=lf
```
3. Ensured scripts are given execute permissions (`chmod +x scripts/*.sh`).

---

## 7. Ansible Community YAML Callback Deprecation & Terminal Warnings

### Symptom:
Executing Ansible playbooks in WSL2 Ubuntu threw:
`[WARNING]: Disabling stdout callback plugin, plugin community.general.yaml not found`

### Root Cause:
Ansible Core 2.20 removed the external `community.general.yaml` callback plugin from the minimal core package distribution unless installed via `ansible-galaxy`.

### Solution:
Updated [`ansible/ansible.cfg`](../ansible/ansible.cfg):
```ini
[defaults]
stdout_callback = default
deprecation_warnings = False
interpreter_python = auto_silent
```
This restored standard stdout reporting with zero warning overhead.

---

## 8. Ansible World-Writable Directory Security Warning on WSL Mounts

### Symptom:
Ansible emitted warnings stating that `ansible.cfg` in current directory was ignored because the `/mnt/c/...` path was owned by a world-writable Windows 777 mount.

### Root Cause:
WSL2 mounts Windows drives with default DrvFs permission masks `0777`, causing Ansible to treat the local configuration file as insecure.

### Solution:
Enforced configuration loading across all automation scripts by explicitly exporting the environment variable:
```bash
export ANSIBLE_CONFIG="$ANSIBLE_DIR/ansible.cfg"
```
Ensured deterministic configuration discovery regardless of mount permissions.

---

## 9. Systemd vs Non-Systemd Dual-Target Compatibility

### Symptom:
Ansible tasks targeting Docker containers failed when attempting to invoke `systemd: name=ampt state=started` because Docker containers lack an init system (PID 1 systemd).

### Root Cause:
The environment contains two valid deployment targets:
1. Native WSL2 Linux with full Systemd support.
2. Lightweight Docker SSH target containers running OpenSSH daemon.

### Solution:
Designed role tasks in `ampt_app/tasks/main.yml` with dual execution paths using `when: ansible_service_mgr == 'systemd'` vs `when: ansible_service_mgr != 'systemd'`.
- On systemd nodes: Deploys `/etc/systemd/system/ampt.service` and uses `ansible.builtin.systemd`.
- On container nodes: Deploys `ampt-runner.sh` (a SysV-compatible daemon manager) and manages background processes via PID files with idempotency guards (`changed_when`).

---

## 10. Atomic Symlink Deployment & Safe Rollback Handling

### Symptom:
In-place overwriting of `ampt.war` during release caused temporary JVM file locking and class-loading errors before service restarts.

### Root Cause:
Operating system file locks prevented direct replacement of active Java executable archives while the JVM process was running.

### Solution:
Implemented an immutable versioned directory structure:
```text
/opt/ampt/releases/<app_version>/ampt.war
/opt/ampt/current -> /opt/ampt/releases/<app_version>
```
Deployments write to a new version directory, and the symlink is atomically updated before restarting the service. Rollbacks require no rebuilds or re-downloads: `rollback.yml` simply points `/opt/ampt/current` back to the previous stable release directory and restarts the service.
