pipeline {
    agent any
    
    tools {
        jdk 'jdk-21.0.12'
        maven 'Maven-3.9.16'
    }
    
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'test', 'prod'], description: 'Target deployment environment')
        string(name: 'SERVER_PORT', defaultValue: '8080', description: 'Tomcat Server Port (Change if Tomcat is on 8081)')
        string(name: 'DEPLOY_PATH', defaultValue: 'C:\\Program Files\\Apache Software Foundation\\Tomcat 10.1\\webapps', description: 'Path to Tomcat webapps directory')
    }
    
    environment {
        APP_NAME = 'ampt'
        APP_URL = "http://localhost:${params.SERVER_PORT}/${APP_NAME}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo "Building for environment: ${params.ENVIRONMENT}"
                bat 'mvn clean package -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }
        
        stage('Package') {
            steps {
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }
        
        stage('Deploy') {
            steps {
                echo "Deploying ${APP_NAME}.war to Tomcat webapps path: ${params.DEPLOY_PATH}"
                // Copy the war file to Tomcat's webapps directory. Tomcat will auto-deploy it.
                bat """
                copy /Y target\\ampt.war "${params.DEPLOY_PATH}\\${APP_NAME}.war"
                """
                echo "Deployment copied! Tomcat will now unpack and deploy."
            }
        }
        
        stage('Health Check') {
            steps {
                echo "Waiting for Tomcat to unpack and start the application..."
                sleep time: 15, unit: 'SECONDS'
                echo "Checking health at ${APP_URL}"
                powershell """
                try {
                    \$response = Invoke-WebRequest -Uri "${APP_URL}" -UseBasicParsing
                    if (\$response.StatusCode -eq 200) {
                        Write-Host "Health check passed. Application is live!"
                    } else {
                        Write-Error "Health check failed with status: \$(\$response.StatusCode)"
                        exit 1
                    }
                } catch {
                    Write-Error "Health check failed: \$_"
                    exit 1
                }
                """
            }
        }
    }
    
    post {
        always {
            echo "Pipeline finished executing."
            junit 'target/surefire-reports/*.xml'
        }
        success {
            echo "Build and Deployment Successful! Application is accessible."
        }
        failure {
            echo "Pipeline Failed during Build, Test, or Deployment."
        }
    }
}
