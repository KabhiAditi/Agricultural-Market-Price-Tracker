# Task 07: Jenkins CI Setup

## 1. Prerequisites and Installation (Windows)
*Note: Since Jenkins is already running on port 8080 locally, you can skip the base installation steps, but ensure the following configuration is complete.*

*   **Unlock Jenkins:** If it's a fresh install, unlock using the password from `C:\ProgramData\Jenkins\.jenkins\secrets\initialAdminPassword`.
*   **Plugins to Install:**
    *   Install the standard suggested plugins.
    *   Go to **Manage Jenkins > Plugins > Available plugins** and search/install these additional plugins: **Maven Integration**, **JUnit**, **HTML Publisher**, and **Docker Pipeline**. Restart Jenkins if prompted.

## 2. Global Tool Configuration
Go to **Manage Jenkins > Tools** (or Global Tool Configuration in older versions) and configure:

*   **JDK Installations:**
    *   Click **Add JDK**.
    *   Uncheck "Install automatically" (unless you have Oracle credentials set).
    *   Name: `JDK21`
    *   JAVA_HOME: Enter the path to your Java 21 folder (e.g., `C:\Program Files\Java\jdk-21`).
*   **Maven Installations:**
    *   Click **Add Maven**.
    *   Uncheck "Install automatically" (unless you want Jenkins to download it).
    *   Name: `Maven3`
    *   MAVEN_HOME: Enter the path to your Maven 3.9 folder (or allow automatic installation from Apache).
*   **Save** your changes.

## 3. Creating the `ampt-ci` Job
Follow these click-by-click steps to create the build job:

1.  From the Jenkins dashboard, click **New Item** (top left).
2.  Enter the item name: `ampt-ci`.
3.  Select **Freestyle project** and click **OK**.
4.  **Source Code Management (SCM) section:**
    *   Select **Git**.
    *   Repository URL: `https://github.com/KabhiAditi/Agricultural-Market-Price-Tracker.git`
    *   Branches to build / Branch Specifier: Change from `*/master` to `*/develop`.
5.  **Build Triggers section:**
    *   Check **Poll SCM**.
    *   In the Schedule box, enter `H/2 * * * *` (This tells Jenkins to check GitHub for new commits every 2 minutes).
    *   *Note for future:* If Jenkins was publicly exposed to the internet (e.g., using ngrok), checking "GitHub hook trigger for GITScm polling" and setting up a webhook on GitHub is a more efficient approach.
6.  **Build Steps section:**
    *   Click **Add build step** and select **Invoke top-level Maven targets**.
    *   Maven Version: Select `Maven3` (the one you named earlier).
    *   Goals: `clean package`
7.  **Post-build Actions section:**
    *   Click **Add post-build action** -> **Archive the artifacts**.
    *   Files to archive: `target/*.war`
    *   Click **Add post-build action** -> **Publish JUnit test result report**.
    *   Test report XMLs: `target/surefire-reports/*.xml`
8.  Click **Save** at the bottom.

## 4. How to Prove the Trigger Works
1.  Open your command prompt or terminal in the project repository.
2.  Switch to develop: `git checkout develop`
3.  Make a trivial change, for instance, add a space or a comment to `README.md`.
4.  Commit the change: `git commit -am "chore: trigger jenkins ci build"`
5.  Push it: `git push origin develop`
6.  Go to the Jenkins dashboard and wait up to 2 minutes. You will see a new build start automatically!

## 5. Evidence Checklist
Capture the following screenshots and save them in the `docs/evidence/task07/` folder:
- [ ] `job_config.png` - Showing the SCM Git repo and Polling Schedule.
- [ ] `polling_log.png` - From the Jenkins build menu showing Git detected a change.
- [ ] `success_console.png` - The bottom of the Console Output showing `BUILD SUCCESS` and finished status.
- [ ] `archived_artifact.png` - The job dashboard showing the blue success orb and `ampt.war` listed under Build Artifacts.
