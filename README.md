# Agricultural Market Price Tracker (AMPT)

CI/CD Pipeline and Application for an Agricultural Market Price Tracker.

**Tech Stack**: Java 21, Spring Boot 3.3.4, Spring MVC, Spring Data JPA, Thymeleaf, H2 (local) / MySQL (profile), Maven, WAR packaging deployable to Tomcat.

---

## Prerequisites
- JDK 21 (configured in `JAVA_HOME` and `PATH`)
- Apache Maven 3.9+
- Git

---

## How to Build & Run Locally

### 1. Build the Package & Run Tests
```bash
mvn clean package
```
- Compiles the application source code.
- Runs JUnit 5 test suite (`AmptApplicationTests`).
- Produces `target/ampt.war` (both standalone runnable and deployable to external Tomcat).

### 2. Start the Local Server
```bash
mvn spring-boot:run
```
- Starts embedded Tomcat on **Port `8082`** (customized to prevent conflicts with Jenkins on `8080` and standalone Tomcat on `8081`).
- Mounts H2 file database at `./data/amptdb` and seeds initial market & commodity data.

### 3. Access in Browser
- **AMPT Dashboard**: [http://localhost:8082/](http://localhost:8082/)
- **Actuator Health Check**: [http://localhost:8082/actuator/health](http://localhost:8082/actuator/health)
- **H2 Database Web Console**: [http://localhost:8082/h2-console](http://localhost:8082/h2-console)
  - JDBC URL: `jdbc:h2:file:./data/amptdb`
  - User: `sa`
  - Password: *(leave blank)*

---

## Running with MySQL Profile (for Docker / CI Environments)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql \
  -DDB_URL=jdbc:mysql://localhost:3306/amptdb -DDB_USER=ampt -DDB_PASSWORD=ampt
```

---

## Documentation
- [Task 3 Architecture & SRS](file:///docs/03-architecture.md)
- [Evidence Logs & HTTP Dumps](file:///docs/evidence/task03/)
