# AMPT — Local Setup

Spring Boot 3 / Java 21 / Maven / H2 (local) / MySQL (profile) / WAR-deployable to Tomcat.

## Prerequisites
- JDK 21
- Maven 3.9+

## Run locally (embedded Tomcat, H2 file DB)
```bash
mvn spring-boot:run
```
Then open http://localhost:8080/ — the dashboard page confirms the DB connection and
shows seeded market/commodity counts.

H2 console (optional, for inspecting data): http://localhost:8080/h2-console
(JDBC URL: `jdbc:h2:file:./data/amptdb`, user `sa`, empty password)

## Build a deployable WAR (for Tomcat — Task 8)
```bash
mvn clean package
# -> target/ampt.war
```
Copy `target/ampt.war` into a standalone Tomcat's `webapps/` directory, or let the
Task 8 Jenkins pipeline do it automatically.

## Run with MySQL profile (used from Task 11 onward)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql \
  -DDB_URL=jdbc:mysql://localhost:3306/amptdb -DDB_USER=ampt -DDB_PASSWORD=ampt
```

## Run tests
```bash
mvn test
```

## Health check
http://localhost:8080/actuator/health — used by the Ansible health-check step in Task 14.

> Note: this skeleton was authored and reviewed for correctness in an environment
> without Maven/internet access to execute an actual build. Before Task 4 (repo
> push) / Task 5, run `mvn clean package` locally once to confirm a clean build —
> the code uses only standard, current Spring Boot 3.3.x APIs.
