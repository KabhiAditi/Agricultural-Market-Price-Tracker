pipeline {
    agent any
    
    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9.16'
    }
    
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'test', 'prod'], description: 'Target deployment environment')
        string(name: 'SERVER_PORT', defaultValue: '8080', description: 'Tomcat Server Port (Change if Tomcat is on 8081)')
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
                script {
                    echo 'Waiting for Tomcat application to become ready...'
                    sleep(time: 15, unit: 'SECONDS')

                    def statusCode = bat(
                        script: '''
                            @echo off
                            curl.exe -s -o health-response.txt -w "%%{http_code}" http://localhost:8080/ampt/actuator/health
                        ''',
                        returnStdout: true
                    ).trim()

                    echo "Health Check HTTP Status: ${statusCode}"

                    if (statusCode != '200') {
                        bat 'type health-response.txt'
                        error("Health check failed. HTTP status: ${statusCode}")
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
