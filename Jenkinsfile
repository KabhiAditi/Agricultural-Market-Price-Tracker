pipeline {
    agent any
    
    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9.16'
    }
    
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'test', 'prod'], description: 'Target deployment environment (local/dev/prod profiles)')
        string(name: 'DOCKERHUB_USER', defaultValue: 'aditiprakash21', description: 'Docker Hub Username / Registry Namespace')
        string(name: 'IMAGE_NAME', defaultValue: 'ampt', description: 'Docker Repository Name')
        string(name: 'CONTAINER_PORT', defaultValue: '8085', description: 'Host port to map container 8080')
        choice(name: 'DEPLOY_MODE', choices: ['docker', 'tomcat'], description: 'Deployment Target: Docker Container (Task 12) or External Tomcat (Task 8)')
        string(name: 'SERVER_PORT', defaultValue: '8081', description: 'Tomcat Server Port (for tomcat deploy mode)')
        string(name: 'DEPLOY_PATH', defaultValue: 'C:\\Tomcat 11.0\\webapps', description: 'Path to Tomcat webapps (for tomcat deploy mode)')
    }
    
    environment {
        APP_NAME = 'ampt'
        CHROME_BIN = 'C:\\Users\\91976\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe'
        CHROMEDRIVER_PATH = 'C:\\Users\\91976\\.cache\\selenium\\chromedriver\\win64\\154.0.8037.92\\chromedriver.exe'
        FULL_IMAGE = "${params.DOCKERHUB_USER}/${params.IMAGE_NAME}"
        BUILD_TAG = "${params.DOCKERHUB_USER}/${params.IMAGE_NAME}:${BUILD_NUMBER}"
        LATEST_TAG = "${params.DOCKERHUB_USER}/${params.IMAGE_NAME}:latest"
        ROLLBACK_TAG = "${params.DOCKERHUB_USER}/${params.IMAGE_NAME}:rollback"
        CONTAINER_NAME = 'ampt-app-prod'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo "Building application artifact for environment: ${params.ENVIRONMENT}"
                bat 'mvn clean package -DskipTests'
            }
        }
        
        stage('Unit & UI Tests') {
            steps {
                echo "Running Unit Tests..."
                bat 'mvn test'
                
                echo "Running Headless Chrome Selenium Acceptance Tests (Quality Gate)..."
                bat """
                    mvn verify -Pui-tests "-Dwebdriver.chrome.driver=${CHROMEDRIVER_PATH}" "-Dchrome.bin=${CHROME_BIN}" "-Dwdm.cachePath=C:\\Users\\91976\\.cache\\selenium"
                """
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml,target/failsafe-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
                }
            }
        }
        
        stage('Package') {
            steps {
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }
        
        stage('Docker Build') {
            when {
                expression { params.DEPLOY_MODE == 'docker' && currentBuild.currentResult == 'SUCCESS' }
            }
            steps {
                script {
                    echo "Building Docker image: ${BUILD_TAG} and ${LATEST_TAG}"
                    bat """
                        docker build -t ${BUILD_TAG} -t ${LATEST_TAG} .
                    """
                }
            }
        }
        
        stage('Docker Push') {
            when {
                expression { params.DEPLOY_MODE == 'docker' && currentBuild.currentResult == 'SUCCESS' }
            }
            steps {
                script {
                    echo "Logging into registry and pushing ${BUILD_TAG} & ${LATEST_TAG}..."
                    // Uses Jenkins credential 'dockerhub-creds' (Username with password / Access Token)
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DH_USER', passwordVariable: 'DH_TOKEN')]) {
                        bat """
                            @echo off
                            echo Logging in to Docker Hub...
                            echo %DH_TOKEN% | docker login -u %DH_USER% --password-stdin
                            if errorlevel 1 exit /b 1
                            echo Pushing build-tagged image...
                            docker push ${BUILD_TAG}
                            if errorlevel 1 exit /b 1
                            echo Pushing latest image...
                            docker push ${LATEST_TAG}
                            if errorlevel 1 exit /b 1
                            echo Image push completed successfully.
                        """
                    }
                }
            }
        }
        
        stage('Deploy Container') {
            when {
                expression { params.DEPLOY_MODE == 'docker' && currentBuild.currentResult == 'SUCCESS' }
            }
            steps {
                script {
                    echo "Deploying Docker container: ${CONTAINER_NAME} on port ${params.CONTAINER_PORT}..."
                    bat """
                        @echo off
                        REM If an existing container is running, tag its image as rollback backup before stopping
                        docker inspect ${CONTAINER_NAME} >nul 2>&1
                        if %errorlevel% equ 0 (
                            echo Tagging currently running image for safe rollback...
                            for /f "tokens=*" %%i in ('docker inspect --format "{{.Config.Image}}" ${CONTAINER_NAME}') do (
                                echo Backing up current running image %%i to ${ROLLBACK_TAG}...
                                docker tag %%i ${ROLLBACK_TAG}
                            )
                            echo Stopping and removing old container ${CONTAINER_NAME}...
                            docker stop ${CONTAINER_NAME}
                            docker rm ${CONTAINER_NAME}
                        )
                        
                        echo Running new container from ${BUILD_TAG}...
                        docker run -d ^
                            --name ${CONTAINER_NAME} ^
                            -p ${params.CONTAINER_PORT}:8080 ^
                            -e SPRING_PROFILES_ACTIVE=${params.ENVIRONMENT} ^
                            -e SERVER_PORT=8080 ^
                            --restart unless-stopped ^
                            ${BUILD_TAG}
                        if errorlevel 1 exit /b 1
                        echo Container ${CONTAINER_NAME} started successfully.
                    """
                }
            }
        }
        
        stage('Smoke Test') {
            when {
                expression { params.DEPLOY_MODE == 'docker' && currentBuild.currentResult == 'SUCCESS' }
            }
            steps {
                script {
                    echo "Performing Smoke Test on http://localhost:${params.CONTAINER_PORT}/actuator/health..."
                    
                    // 12 retries x 5 seconds = 60 seconds max wait for container Spring Boot boot
                    retry(12) {
                        sleep(time: 5, unit: 'SECONDS')
                        
                        def statusCode = bat(
                            script: """
                                @echo off
                                curl.exe -s -o smoke-response.txt -w "%%{http_code}" http://localhost:${params.CONTAINER_PORT}/actuator/health
                            """,
                            returnStdout: true
                        ).trim()
                        
                        echo "Smoke Test HTTP Status: ${statusCode}"
                        
                        if (statusCode != '200') {
                            error("Smoke test failed: application returned HTTP ${statusCode}")
                        }
                    }
                    
                    echo "Smoke Test PASSED! Actuator Health response:"
                    bat 'type smoke-response.txt'
                }
            }
        }
        
        // Retain Tomcat deploy stage if DEPLOY_MODE == 'tomcat'
        stage('Deploy Tomcat') {
            when {
                expression { params.DEPLOY_MODE == 'tomcat' && currentBuild.currentResult == 'SUCCESS' }
            }
            steps {
                echo "Deploying ${APP_NAME}.war to Tomcat webapps: ${params.DEPLOY_PATH}"
                bat """
                    @echo off
                    if exist "${params.DEPLOY_PATH}\\${APP_NAME}" (
                        echo Removing old exploded app directory...
                        rmdir /s /q "${params.DEPLOY_PATH}\\${APP_NAME}"
                    )
                    echo Copying new WAR to ${params.DEPLOY_PATH}...
                    copy /Y target\\ampt.war "${params.DEPLOY_PATH}\\${APP_NAME}.war"
                    echo WAR copied successfully.
                """
            }
        }
        
        stage('Health Check Tomcat') {
            when {
                expression { params.DEPLOY_MODE == 'tomcat' && currentBuild.currentResult == 'SUCCESS' }
            }
            steps {
                script {
                    echo 'Checking application health on Tomcat...'
                    retry(18) {
                        sleep(time: 10, unit: 'SECONDS')
                        def statusCode = bat(
                            script: """
                                @echo off
                                curl.exe -s -o health-response.txt -w "%%{http_code}" http://localhost:${params.SERVER_PORT}/ampt/actuator/health
                            """,
                            returnStdout: true
                        ).trim()
                        echo "Health Check HTTP Status: ${statusCode}"
                        if (statusCode != '200') {
                            error("Application not ready yet. HTTP status: ${statusCode}")
                        }
                    }
                    echo 'Application health check passed!'
                    bat 'type health-response.txt'
                }
            }
        }
    }
    
    post {
        always {
            echo "Pipeline execution finished."
            junit testResults: 'target/surefire-reports/*.xml,target/failsafe-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
        }
        success {
            echo "CI/CD Pipeline Successful! Continuous Deployment to Docker container verified."
        }
        failure {
            echo "Pipeline Failed! Deployment prevented by Quality Gate or deployment error."
        }
    }
}
