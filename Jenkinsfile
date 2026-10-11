pipeline {
    agent any
    
    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9.16'
    }
    
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'test', 'prod'], description: 'Target deployment environment')
        string(name: 'SERVER_PORT', defaultValue: '8081', description: 'Tomcat Server Port (Change if Tomcat is on 8081)')
        string(name: 'DEPLOY_PATH', defaultValue: 'C:\\Program Files\\Apache Software Foundation\\Tomcat 11.0\\webapps', description: 'Path to Tomcat webapps directory')
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
        
        stage('Deploy') {
            when {
                expression { currentBuild.currentResult == 'SUCCESS' }
            }
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
                script {
                    echo 'Checking application health on Tomcat...'

                    retry(6) {
                        sleep(time: 10, unit: 'SECONDS')

                        def statusCode = bat(
                            script: '''
                                @echo off
                                curl.exe -s -o health-response.txt -w "%%{http_code}" http://localhost:8081/ampt/actuator/health
                            ''',
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
            echo "Pipeline finished executing."
            junit testResults: 'target/surefire-reports/*.xml,target/failsafe-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
        }
        success {
            echo "Build, Test Quality Gate, and Deployment Successful! Application is verified."
        }
        failure {
            echo "Pipeline Failed! Deployment was prevented by the Quality Gate."
        }
    }
}
