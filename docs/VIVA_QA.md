# DevOps Project Viva & Oral Examination Q&A (30 Questions)

Comprehensive technical questions and authoritative answers covering the concepts, tools, and practices implemented in the **Agricultural Market Price Tracker (AMPT)** project.

---

## Section 1: Git & Version Control

### Q1: What branching strategy was used in this project, and why?
**Answer:** We implemented **GitFlow**. It uses two long-lived branches: `main` (production-ready code) and `develop` (integration branch for features). New features originate from `develop` as `feature/<task-name>`, are reviewed via Pull Requests, and merged back into `develop`. Releases and hotfixes are merged into both `main` and `develop`. This isolates unfinished work, allows parallel development, and prevents unstable code from reaching production.

### Q2: What is the difference between `git merge` and `git rebase`?
**Answer:** `git merge` takes the contents of a source branch and integrates them into the target branch by creating a new 3-way "merge commit". It preserves the exact chronological history and context of the branch. `git rebase` moves the base of the current branch onto another commit by replaying commits sequentially. It creates a linear history without merge commits but rewrites commit SHAs. In team projects, merge is favored on public shared branches like `develop` to preserve audit trails.

### Q3: What is the difference between a soft, mixed, and hard Git reset?
**Answer:**
- `git reset --soft <commit>`: Moves `HEAD` to the target commit; leaves the staging area (index) and working directory unchanged.
- `git reset --mixed <commit>` (default): Moves `HEAD` and unstages changes; leaves the working directory intact.
- `git reset --hard <commit>`: Moves `HEAD`, resets the staging area, and wipes all uncommitted modifications in the working directory to match the target commit.

### Q4: Why is `.gitignore` important in automated CI/CD pipelines?
**Answer:** `.gitignore` prevents machine-specific, temporary, or sensitive files (e.g., compiled `.class` files, `target/` directories, IDE caches `.idea/`, `.env` credentials, and OS metadata) from being checked into the repository. This keeps repo clone sizes small, prevents secret leakage, and guarantees that Jenkins builds clean artifacts from raw source code rather than relying on developer machine artifacts.

---

## Section 2: Build Automation with Maven

### Q5: What is the Maven Build Lifecycle, and what are its standard phases?
**Answer:** The Maven build lifecycle is an ordered sequence of phases. Running a later phase automatically executes all preceding phases. The standard default lifecycle is:
1. `validate` (validates project structure)
2. `compile` (compiles source code)
3. `test` (executes unit tests using Surefire)
4. `package` (packages compiled classes into a distributable format like JAR/WAR)
5. `verify` (runs integration tests using Failsafe and quality checks)
6. `install` (installs package to local repository `~/.m2`)
7. `deploy` (publishes artifact to remote Nexus/Artifactory repository).

### Q6: What is the difference between Maven Surefire and Failsafe plugins?
**Answer:**
- **Maven Surefire (`mvn test`)**: Designed for isolated unit tests. If any unit test fails, the build terminates immediately in the `test` phase.
- **Maven Failsafe (`mvn verify`)**: Designed for integration tests that require a running server environment. It has two phases: `integration-test` (runs tests) and `verify` (checks results). If an integration test fails during `integration-test`, Failsafe allows subsequent teardown goals (like shutting down Spring Boot or Tomcat) to execute before failing the build in `verify`.

### Q7: What are Maven Profiles, and how did you use them in this project?
**Answer:** Profiles allow conditional customization of plugins, dependencies, and properties based on the execution environment. In AMPT, we configured a profile `ui-tests` in `pom.xml`. Regular builds (`mvn test`) run fast unit tests without launching a web browser. When passing `-Pui-tests`, Maven activates the Failsafe plugin, spins up the Spring Boot test instance on a dynamic port, and launches Chrome for Selenium tests.

---

## Section 3: Continuous Integration with Jenkins

### Q8: What is Continuous Integration (CI)?
**Answer:** Continuous Integration is the software development practice where developers frequently merge their code changes into a central repository (multiple times a day). Each integration is automatically verified by an automated build and test pipeline. The goal is to detect integration defects early, prevent "merge hell", and shorten feedback cycles.

### Q9: What is the difference between Declarative and Scripted Jenkins Pipelines?
**Answer:**
- **Declarative Pipeline**: Provides a structured, strict syntax enclosed in a `pipeline { ... }` block. It provides built-in validation, stages, steps, environment definitions, and `post` handlers, making pipelines readable and standardized.
- **Scripted Pipeline**: Uses arbitrary Groovy code inside `node { ... }`. It offers unlimited flexibility for complex algorithmic workflows but is harder to maintain and lacks out-of-the-box structural constraints.

### Q10: How does Jenkins manage credentials securely?
**Answer:** Jenkins stores credentials in an encrypted system keystore (`Credentials Store`). In declarative pipelines, credentials are never hardcoded in text. Instead, they are referenced by a unique ID using bindings such as `withCredentials([usernamePassword(...)])` or `string(...)`. Jenkins automatically masks these credentials in console logs (`****`) to prevent leakage.

### Q11: What are Quality Gates in a CI/CD pipeline?
**Answer:** A Quality Gate is an automated enforcement checkpoint that evaluates build metrics against predefined acceptance criteria before allowing the pipeline to proceed to packaging or deployment. Examples include: zero test failures, minimum code coverage threshold (e.g., Jacoco 80%), zero critical SonarQube bugs, and passing end-to-end Selenium acceptance journeys.

---

## Section 4: Automated Testing with Selenium

### Q12: What is the Page Object Model (POM) pattern in Selenium?
**Answer:** The Page Object Model is a design pattern that models each web page or UI component as an object/class. The class encapsulates web elements (locators like `By.id`, `By.xpath`) and page actions (methods like `login()`, `submitPriceForm()`), while the test classes contain only test logic and assertions. This decouples test logic from HTML structure: if an element locator changes, only the Page Object is updated, preventing fragile tests.

### Q13: Why is Headless Mode essential for browser testing in CI pipelines?
**Answer:** CI servers (like Jenkins running in Docker, headless Linux nodes, or Windows services) run without a physical graphical display or X11 server. Headless mode instructs the browser engine (e.g., Chrome `--headless=new`) to render pages, execute JavaScript, and compute DOM trees in memory without launching a visible window, enabling UI automation on servers.

### Q14: How do you handle asynchronous loading in Selenium tests?
**Answer:** We avoid hardcoded `Thread.sleep()` because it causes flaky or slow tests. Instead, we use **Explicit Waits** via `WebDriverWait` and `ExpectedConditions` (e.g., `ExpectedConditions.visibilityOfElementLocated(...)` or `elementToBeClickable(...)`). The driver polls the DOM at set intervals until the condition evaluates to true or a maximum timeout is reached.

---

## Section 5: Docker & Containerization

### Q15: What is the fundamental difference between a Virtual Machine (VM) and a Docker Container?
**Answer:** A VM virtualizes hardware and runs a complete guest operating system with its own kernel, device drivers, and heavy memory footprint (gigabytes). A Docker container virtualizes at the OS level, sharing the host operating system kernel while isolating processes, filesystem, network, and memory via Linux kernel namespaces and cgroups. Containers are lightweight (megabytes) and boot in milliseconds.

### Q16: Why should production containers NEVER run as the root user?
**Answer:** Running as `root` inside a container violates the principle of least privilege. If a remote code execution (RCE) vulnerability or container breakout occurs, the attacker inherits root capabilities on the host kernel. In AMPT, our `Dockerfile` creates a dedicated system group and user `ampt` (UID 10001) and specifies `USER ampt`.

### Q17: What is the purpose of the `HEALTHCHECK` instruction in a Dockerfile?
**Answer:** By default, Docker only checks if the container's PID 1 process is running. However, a Java process might be alive while Tomcat is deadlocked or out of memory. The `HEALTHCHECK` instruction runs a command inside the container (e.g., `curl -f http://localhost:8080/actuator/health || exit 1`) at periodic intervals. Docker inspects the return code to mark the container as `healthy`, `unhealthy`, or `starting`.

### Q18: What is a Docker multi-stage build, and what advantages does it provide?
**Answer:** A multi-stage build uses multiple `FROM` instructions in a single `Dockerfile`. Earlier stages act as build environments with heavy tools (JDK, Maven, compilers), while the final stage copies only the compiled output (e.g., `ampt.war`) into a minimal runtime base (e.g., JRE Alpine or Jammy). This dramatically reduces image size, removes build tools from production images, and reduces the security attack surface.

---

## Section 6: Configuration Management with Ansible

### Q19: What is Configuration Management, and how does Ansible differ from Puppet/Chef?
**Answer:** Configuration Management is the practice of maintaining computer systems, servers, and software in a desired, consistent state automatically through code. Ansible is **agentless** and **push-based**: it connects over standard SSH (or WinRM/local) using Python and requires no daemon on target nodes. Puppet and Chef are agent-based and pull-based, requiring specialized agent software and master servers.

### Q20: What is Idempotency in Configuration Management? Why is it crucial?
**Answer:** An operation is **idempotent** if applying it multiple times produces the exact same result as applying it once, without unintended side effects. In Ansible, running a playbook on an already configured node makes zero changes (`changed=0`). This ensures consistency, prevents configuration drift, allows safe execution at any time, and ensures services are not restarted unless configuration actually changed.

### Q21: What are Ansible Roles, and how are they organized?
**Answer:** Roles provide a standardized directory structure to organize modular, reusable automation logic. Standard role subdirectories include:
- `tasks/`: Main execution task lists.
- `handlers/`: Event-triggered tasks (e.g., service restarts when config files change).
- `templates/`: Jinja2 `.j2` configuration templates.
- `defaults/`: Default variables with lowest precedence.
- `vars/`: Variables with high precedence.
- `meta/`: Role metadata and dependencies.

### Q22: What are Ansible Handlers, and when do they execute?
**Answer:** Handlers are special tasks that only execute when notified by another task that reported a `changed` status. For example, updating `application.yml` sends `notify: Restart ampt service`. By default, handlers run once at the very end of the play (or when forced via `meta: flush_handlers`), preventing redundant service restarts if multiple config files change in the same run.

---

## Section 7: CI vs CD vs Continuous Deployment

### Q23: Clearly explain the differences between CI, Continuous Delivery, and Continuous Deployment.
**Answer:**
- **Continuous Integration (CI)**: Automates building and testing code on every commit to verify that new code integrates cleanly without breaking existing functionality.
- **Continuous Delivery (CD)**: Automatically tests, packages, and deploys the build into staging/testing environments, ensuring that every build is in a deployable, release-ready state. Production deployment is triggered manually with a single click after business approval.
- **Continuous Deployment**: Extends Continuous Delivery by removing the manual human gate. Every change that passes all automated quality gates is deployed directly and automatically to production with zero human intervention.

### Q24: What deployment strategies minimize or eliminate downtime?
**Answer:**
1. **Blue/Green Deployment**: Two identical production environments exist. Blue runs live traffic; Green receives the new release. After smoke tests pass on Green, router/load balancer traffic switches from Blue to Green instantly.
2. **Canary Deployment**: The new version is deployed to a tiny subset of servers (e.g., 5% of users). Once metrics prove stability, the release rolls out to remaining servers.
3. **Rolling Update**: Replaces running instances one by one (or in small batches) behind a load balancer until all instances are updated.
4. **Symlink Swapping**: On single-node architectures (used in AMPT Task 14/15), atomic symlink updating points `/opt/ampt/current` to the new release folder before restarting the daemon.

---

## Section 8: Reliability, Health Monitoring, & Rollback

### Q25: What is Spring Boot Actuator, and why is `/actuator/health` preferred for monitoring?
**Answer:** Spring Boot Actuator provides production-ready operational management features for applications over HTTP/JMX. The `/actuator/health` endpoint aggregates deep subsystem health checks (database connectivity via `isValid()`, disk space thresholds, ping, readiness, and liveness states) and returns an HTTP status code (200 for `UP`, 503 for `DOWN`) along with structured JSON, making it ideal for container health checks, load balancers, and CI smoke tests.

### Q26: How does automated rollback work in Docker-based CD?
**Answer:** Before stopping or replacing a running container, the CD pipeline captures the image identifier of the currently running container (`docker inspect --format '{{.Config.Image}}' <container>`) and tags it with a dedicated backup tag (e.g., `ampt:rollback`). If the new container fails its health checks or smoke test, the pipeline immediately runs a rollback script that reinstantiates the previous container from the `ampt:rollback` image, restoring service within seconds.

### Q27: How does automated rollback work in Ansible-managed hosts?
**Answer:** We decouple application binaries from service unit files by storing versioned releases under `/opt/ampt/releases/<version>/` and running the application through an atomic symlink `/opt/ampt/current`. When `rollback.yml` is executed with `-e previous_version=v1.0.0`, Ansible checks that the folder exists, switches the symlink atomically, restarts systemd, and polls `/actuator/health` until UP. No rebuilds or network downloads are needed.

### Q28: What is the purpose of Spring Boot's Liveness and Readiness state probes?
**Answer:**
- **Liveness Probe**: Determines if the application internal state is healthy. If liveness fails (e.g., deadlock or unrecoverable state), the container orchestrator restarts the container.
- **Readiness Probe**: Determines if the application is ready to accept external network traffic. If readiness is false (e.g., database connection pool warming up or caches loading), traffic is diverted away from the instance until it reports ready.

### Q29: What is "Configuration Drift", and how does Ansible eliminate it?
**Answer:** Configuration Drift occurs when manual, ad-hoc changes (e.g., editing files via `nano`, installing packages manually, or tweaking firewall rules directly on the server) cause server state to diverge over time from documented infrastructure specifications. Ansible eliminates drift because the desired state is version-controlled as code. Running playbooks regularly enforces the declared state and reverts any unauthorized manual deviations.

### Q30: What were the most significant engineering challenges encountered in this project, and how were they resolved?
**Answer:**
1. **Windows Batch Pipe Spacing in Jenkins**: `echo %TOKEN% | docker login` caused unauthorized errors due to trailing spaces before the pipe character; resolved by quoting credentials and leveraging Jenkins `withCredentials` bindings.
2. **Host Disk Space Exhaustion**: 0 MB remaining on host C: drive froze Docker Desktop; resolved by pruning dangling build cache layers and cleaning temporary files.
3. **Database Seed Ordering**: H2 foreign key constraint failures during early boot; resolved by reordering master and transactional inserts in `data.sql` and deferring datasource initialization.
4. **Dynamic Driver Resolution in Selenium**: Browser version mismatch broke UI tests; resolved using BoniGarcia's `WebDriverManager` to dynamically detect Chrome versions and download matching ChromeDriver binaries in headless mode.
