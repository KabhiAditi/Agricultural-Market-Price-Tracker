# Task 10: Continuous Testing & Quality Gate in Jenkins

## 1. Overview
This document details the configuration and verification of automated Quality Gates in the AMPT Jenkins CI/CD pipeline. Continuous testing ensures that regressions and broken business logic prevent code from being packaged or deployed to Tomcat.

---

## 2. Pipeline Quality Gate Architecture
The declarative [`Jenkinsfile`](file:///C:/Users/91976/Desktop/Assignments%20Semester%207/DevOps/DevOps%20Project/Jenkinsfile) was updated to enforce a multi-tier test gate:

```
[ Checkout ] ──> [ Build ] ──> [ Unit Tests ] ──> [ Package ] ──> [ UI Tests (Quality Gate) ] ──> [ Deploy (Conditional) ] ──> [ Health Check ]
                                                                       │
                                                         ┌─────────────┴─────────────┐
                                                         ▼                           ▼
                                                      Success                      Failure
                                                         │                           │
                                                         ▼                           ▼
                                                  Deploy to Tomcat           Deploy SKIPPED (Aborted)
                                                                             Screenshots Archived
```

### Key Declarative Constructs
1. **Headless Chrome UI Acceptance Gate (`UI Tests` Stage)**:
   ```groovy
   stage('UI Tests') {
       steps {
           echo "Running Headless Chrome Selenium Acceptance Tests (Quality Gate)..."
           bat 'mvn verify -Pui-tests'
       }
       post {
           always {
               junit testResults: 'target/surefire-reports/*.xml,target/failsafe-reports/*.xml', allowEmptyResults: true
               archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
           }
       }
   }
   ```
2. **Conditional Deployment Barrier**:
   ```groovy
   stage('Deploy') {
       when {
           expression { currentBuild.currentResult == 'SUCCESS' }
       }
       steps {
           echo "Deploying ${APP_NAME}.war to Tomcat webapps path: ${params.DEPLOY_PATH}"
           bat 'copy /Y target\\ampt.war "${params.DEPLOY_PATH}\\${APP_NAME}.war"'
       }
   }
   ```
   If any unit test or Selenium test journey fails, `currentResult` becomes `'UNSTABLE'` or `'FAILURE'`, causing Jenkins to immediately **skip** the `Deploy` and `Health Check` stages.

---

## 3. Deliberate Defect Demonstration (`bugfix/intentional-defect-demo`)

### A. Introduced Defect
*   **Target Component:** [`SummaryService.java`](file:///C:/Users/91976/Desktop/Assignments%20Semester%207/DevOps/DevOps%20Project/src/main/java/com/ampt/service/SummaryService.java)
*   **Defect Injected:** The percentage change formula in `getDailySummaries()` was inverted to divide by `avgModal` (today's price) instead of `prevAvg` (yesterday's baseline price):
    ```diff
    - BigDecimal pct = diff.multiply(BigDecimal.valueOf(100)).divide(prevAvg, 2, RoundingMode.HALF_UP);
    + // INTENTIONAL DEFECT DEMO: Divides by today's price instead of yesterday's baseline
    + BigDecimal pct = diff.multiply(BigDecimal.valueOf(100)).divide(avgModal, 2, RoundingMode.HALF_UP);
    ```

### B. Impact on Pipeline Execution
*   **Test Failure:** Unit test `SummaryServiceTest.testDailySummaries_ComputesAvgAndPercentChange()` fails (`expected: <5.00> but was: <4.76>`).
*   **Quality Gate Result:** Pipeline marks build as **FAILED** at the Test phase.
*   **Deployment Protection:** The `Deploy` and `Health Check` stages are **SKIPPED**. No broken WAR is copied to Apache Tomcat.
*   **Diagnostics:** Jenkins archives JUnit test reports and failure screenshots to `target/screenshots/`.

---

## 4. Defect Resolution (`fix: correct percent change calculation`)

### A. Code Fix Applied
*   Restored correct baseline division by `prevAvg`:
    ```java
    BigDecimal pct = diff.multiply(BigDecimal.valueOf(100))
            .divide(prevAvg, 2, RoundingMode.HALF_UP);
    ```

### B. Resulting Pipeline Execution
*   `mvn test` passes 33/33 tests.
*   `mvn verify -Pui-tests` passes all 5 Selenium user journeys.
*   `Deploy` stage executes and deploys `ampt.war` to `C:\Program Files\Apache Software Foundation\Tomcat 11.0\webapps`.
*   `Health Check` verifies HTTP 200 at `http://localhost:8081/ampt/actuator/health`.
*   All test results are published to the Jenkins Test Result Dashboard.

---

## 5. Evidence Checklist & Screenshots

Save the following evidence artifacts in `docs/evidence/task10/`:

| Artifact Name | Description | Status |
| :--- | :--- | :--- |
| `pipeline-failed-gate.png` | Jenkins Pipeline view showing red failure at Test/UI stage and skipped Deploy stage | [ ] To Capture |
| `test-report-failed.png` | Jenkins JUnit Test Result page showing the failing assertion in `SummaryServiceTest` | [ ] To Capture |
| `pipeline-success-deployed.png` | Jenkins Pipeline view showing all green stages including Deploy and Health Check | [ ] To Capture |
| `tomcat-verified.png` | Browser displaying live AMPT dashboard on Tomcat after green pipeline | [ ] To Capture |
