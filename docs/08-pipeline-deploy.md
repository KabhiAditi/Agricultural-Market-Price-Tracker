# Task 08: Pipeline as Code & Tomcat Deployment

## 1. Tomcat 10.1 Installation & Startup on Windows
To deploy the `ampt.war` artifact, we use Apache Tomcat 10.1.

1.  **Download:** Download the Tomcat 10.1 Windows zip (e.g., `apache-tomcat-10.1.x-windows-x64.zip`) from the official Apache Tomcat website.
2.  **Extract:** Unzip the contents to a directory (e.g., `C:\Program Files\Apache Software Foundation\Tomcat 10.1`).
3.  **Port Configuration (Optional):** If port 8080 is already used by Jenkins, change Tomcat's port to `8081`:
    *   Open `conf/server.xml` in a text editor.
    *   Find the `<Connector port="8080" protocol="HTTP/1.1"...` line.
    *   Change `8080` to `8081`.
    *   Save and close the file.
4.  **Set Spring Profile:** To ensure Tomcat runs the app in the `dev` profile (matching the pipeline parameter):
    *   Create a file called `setenv.bat` in the Tomcat `bin` folder.
    *   Add this line: `set "JAVA_OPTS=-Dspring.profiles.active=dev"`
5.  **Start Tomcat:**
    *   Open a Command Prompt as Administrator.
    *   Navigate to the Tomcat `bin` folder.
    *   Run `startup.bat`.
    *   Tomcat will open in a new console window. Keep it open.

## 2. Jenkins Pipeline Configuration (SCM)
We will now create a new Pipeline job that reads the `Jenkinsfile` from the root of our Git repository.

1.  In Jenkins, click **New Item**.
2.  Name the item `ampt-pipeline` (or similar), select **Pipeline**, and click **OK**.
3.  In the configuration page, check **This project is parameterized** and add:
    *   **Choice Parameter:** Name: `ENVIRONMENT`, Choices: `dev`, `test`, `prod`.
    *   **String Parameter:** Name: `SERVER_PORT`, Default Value: `8081`.
    *   **String Parameter:** Name: `DEPLOY_PATH`, Default Value: `C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps` (Update this if your Tomcat path is different).
4.  Scroll down to the **Pipeline** section.
5.  Change the **Definition** dropdown from "Pipeline script" to **Pipeline script from SCM**.
6.  **SCM:** Select **Git**.
7.  **Repository URL:** Enter your GitHub repo URL (e.g., `https://github.com/KabhiAditi/Agricultural-Market-Price-Tracker.git`).
8.  **Branches to build:** Change the branch specifier to `*/develop` (or `*/main`).
9.  **Script Path:** Leave it as `Jenkinsfile`.
10. Click **Save**.

## 3. Running the Pipeline & Accessing the App
1.  Click **Build with Parameters** on the left menu.
2.  Leave the defaults (ENVIRONMENT=`dev`, SERVER_PORT=`8081`, etc.) and click **Build**.
3.  The pipeline will check out the code, build it with Maven, run tests, package it into a `.war`, and copy it directly to Tomcat's `webapps` folder. The final Health Check stage will ping the server to verify it's running.
4.  Once deployed, Tomcat automatically extracts `ampt.war` and serves it.
5.  **App URL:** Open your browser and go to `http://localhost:8081/ampt` (or your configured port).

## 4. Evidence Checklist for Task 8
Please take screenshots of the following and save them in your Task 8 evidence folder (`docs/evidence/task08/`):
- [ ] `pipeline_stage_view.png` - The Jenkins Pipeline Stage View showing all stages (Checkout, Build, Test, Package, Deploy, Health Check) in green.
- [ ] `tomcat_manager.png` - The Tomcat Manager GUI (or simply the Tomcat console output window) showing the `ampt` application deployed.
- [ ] `app_in_browser.png` - The AMPT application loaded in your web browser via `http://localhost:8081/ampt`.
