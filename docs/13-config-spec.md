# Target Node Configuration Specification (AMPT)

**Document Reference:** `docs/13-config-spec.md`  
**System:** Agricultural Market Price Tracker (AMPT)  
**Configuration Management Tool:** Ansible 2.15+  
**Target Operating System:** Ubuntu 22.04 LTS / Debian 12 (Linux x86_64)

---

## 1. Executive Summary

This document defines the formal system configuration specification required to deploy and operate the **Agricultural Market Price Tracker (AMPT)** on target compute nodes using Ansible automation. Every resource declared in this specification follows declarative, idempotent infrastructure-as-code principles.

```
+-----------------------------------------------------------------------------------+
|                              AMPT Node Architecture                               |
|                                                                                   |
|  [ Ingress Port: 8080 ]                                                           |
|           |                                                                       |
|           v                                                                       |
|  +-----------------------------------------------------------------------------+  |
|  | Service Manager: Systemd (`ampt.service`)                                   |  |
|  | User / Security Context: `ampt:ampt` (Non-root, system daemon)               |  |
|  +-----------------------------------------------------------------------------+  |
|           |                                                                       |
|           +---> Application Root: `/opt/ampt/ampt.war`                            |
|           +---> Configuration:    `/opt/ampt/application.yml` (Jinja2 Template)  |
|           +---> Database Data:    `/opt/ampt/data/` (H2 MVStore file store)       |
|           +---> Log Destination:  `/var/log/ampt/ampt.log`                        |
|           +---> Health Check:     `http://127.0.0.1:8080/actuator/health`         |
+-----------------------------------------------------------------------------------+
```

---

## 2. Target Node Prerequisites

### 2.1 Operating System & Hardware Baseline
- **Supported Distributions:** Ubuntu 20.04/22.04/24.04 LTS, Debian 11/12
- **Kernel:** Linux Kernel 5.15+ (x86_64 / amd64)
- **Minimum Compute:** 1 vCPU, 1 GB RAM, 10 GB free disk space
- **Python Runtime:** Python 3.9+ with standard libraries (`python3`, `python3-apt` / `python3-pip`)

### 2.2 System Packages
The following package dependencies must be installed and verified via the `package` / `apt` Ansible module:

| Package Name | Purpose | Minimum Version | Ansible Module |
|---|---|---|---|
| `openjdk-21-jre-headless` | JVM runtime for running Spring Boot standalone archive | 21.0.x | `ansible.builtin.package` |
| `curl` | Command-line HTTP client used for local health checks and debugging | 7.81.0+ | `ansible.builtin.package` |
| `ca-certificates` | Trusted TLS certificate bundles for secure external outbound calls | Latest | `ansible.builtin.package` |
| `tar` / `gzip` | Archive decompression tools for payload management | Latest | `ansible.builtin.package` |
| `docker.io` *(optional container runtime)* | Alternative container runtime if running AMPT in container mode | 24.0+ | `ansible.builtin.package` |

---

## 3. User, Group & Security Isolation

To comply with the principle of least privilege, the AMPT application never runs as `root`. A dedicated system service account must be provisioned.

### 3.1 Group Specification
- **Group Name:** `ampt`
- **Type:** System Group (`system: yes`)
- **GID:** Auto-allocated system GID (`< 1000`)
- **Ansible Module:** `ansible.builtin.group`

### 3.2 User Specification
- **User Name:** `ampt`
- **Primary Group:** `ampt`
- **Secondary Groups:** None (or `docker` if container runtime profile is activated)
- **System Account:** `yes` (`system: yes`)
- **Home Directory:** `/opt/ampt`
- **Login Shell:** `/usr/sbin/nologin` (interactive SSH shell access prohibited)
- **Create Home:** `no` (directory managed explicitly by directory task)
- **Ansible Module:** `ansible.builtin.user`

---

## 4. Directory Hierarchy & File Permissions

All directories must be created with explicit ownership and strict POSIX file mode permissions to prevent unauthorized access across tenants.

| Directory Path | Owner | Group | Mode | Description |
|---|---|---|---|---|
| `/opt/ampt` | `ampt` | `ampt` | `0750` | Application root containing JAR/WAR binary and runtime artifacts |
| `/opt/ampt/data` | `ampt` | `ampt` | `0750` | Persistent storage directory for file-based H2 database (`amptdb.mv.db`) |
| `/var/log/ampt` | `ampt` | `ampt` | `0755` | Structured application logs, GC logs, and systemd standard output redirection |
| `/etc/ampt` | `ampt` | `ampt` | `0750` | Configuration drop-in directory for environment variable exports |

---

## 5. Configuration Templates

The configuration must be dynamically generated from Jinja2 templates to ensure multi-environment consistency (`dev`, `test`, `prod`).

### 5.1 Application Configuration (`/opt/ampt/application.yml`)
Generated from template `roles/ampt_app/templates/application.yml.j2`:
- **File Mode:** `0640`
- **Owner / Group:** `ampt:ampt`
- **Key Parameters:**
  - `server.port`: Target HTTP port (default `8080`)
  - `spring.profiles.active`: Active environment profile (`dev`, `test`, `prod`, `mysql`)
  - `spring.datasource.url`: JDBC URL (H2 file or MySQL socket)
  - `management.endpoints.web.exposure.include`: Actuator metrics exposure (`health,info,metrics`)

### 5.2 Environment Override File (`/etc/ampt/ampt.env`)
- **File Mode:** `0600`
- **Owner / Group:** `ampt:ampt`
- **Purpose:** Exports environment variables (`SPRING_PROFILES_ACTIVE`, `SERVER_PORT`, `JAVA_OPTS`) to the systemd service without hardcoding secrets.

---

## 6. Service & Process Management (Systemd Unit)

When deployed directly to bare metal or virtual machines, AMPT is managed by Systemd.

### 6.1 Unit Specification (`/etc/systemd/system/ampt.service`)
Generated from `roles/ampt_app/templates/ampt.service.j2`:

```ini
[Unit]
Description=Agricultural Market Price Tracker (AMPT)
Documentation=https://github.com/KabhiAditi/Agricultural-Market-Price-Tracker
After=network.target network-online.target syslog.target
Wants=network-online.target

[Service]
Type=simple
User=ampt
Group=ampt
WorkingDirectory=/opt/ampt
EnvironmentFile=-/etc/ampt/ampt.env
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /opt/ampt/ampt.war
Restart=always
RestartSec=10
TimeoutStopSec=30
StandardOutput=append:/var/log/ampt/ampt.log
StandardError=append:/var/log/ampt/ampt-error.log
SuccessExitStatus=143
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```

### 6.2 Service Lifecycle Controls
- **State:** `started`
- **Enabled on Boot:** `yes`
- **Handler Trigger:** Notified on application binary changes or configuration template updates (`Restart ampt`).

---

## 7. Network & Health Check Specification

### 7.1 Port Exposure
- **Ingress Port:** `8080` (HTTP)
- **Firewall Rule:** Permitted via `ufw allow 8080/tcp` or iptables filter table.

### 7.2 Idempotent Health Verification
Every Ansible deployment must execute an active HTTP verification step:
- **Module:** `ansible.builtin.uri`
- **Target URL:** `http://127.0.0.1:8080/actuator/health`
- **HTTP Method:** `GET`
- **Expected Status Code:** `200`
- **Expected Body Content:** `"status": "UP"`
- **Retry Policy:** 12 retries with 5-second delay (up to 60 seconds warm-up window).

---

## 8. Idempotency Assurance Matrix

| State Action | Ansible Module | Idempotent Guarantee |
|---|---|---|
| Package Installation | `ansible.builtin.package` | No-op if packages already at desired state |
| User / Group Creation | `ansible.builtin.user`, `ansible.builtin.group` | Preserves existing UID/GID without changes |
| Directory Creation | `ansible.builtin.file` | Updates permissions/ownership only if drifting |
| Template Rendering | `ansible.builtin.template` | Compares MD5 checksum; replaces only on diff |
| Service Management | `ansible.builtin.systemd` | No-op if already running and enabled |
| Health Check Validation | `ansible.builtin.uri` | Pure query step; creates no system mutation |
