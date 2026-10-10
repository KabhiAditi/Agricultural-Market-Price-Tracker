pipeline {
    agent any
    
    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9.16'
    }
    
    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'test', 'prod'], description: 'Target deployment environment')
        string(name: 'SERVER_PORT', defaultValue: '8080', description: 'Tomcat Server Port (Change if Tomcat is on 8081)')
        string(name: 'DEPLOY_PATH', defaultValue: 'C:\\Program Files\\Apache Software Foundation\\Tomcat 10.1\\webapps', description: 'Path to Tomcat webapps directory')
    }
    
    environment {
        APP_NAME = 'ampt/'
        APP_URL = "http://localhost:${params.SERVER_PORT}/${APP_NAME}"
    }
    
    
    stage('Health Check') {
        steps {
            echo 'Waiting for Tomcat to deploy the application...'
            sleep(time: 15, unit: 'SECONDS')

            powershell '''
                $url = "http://localhost:8081/ampt/"

                try {
                    $response = Invoke-WebRequest `
                        -Uri $url `
                        -MaximumRedirection 5 `
                        -TimeoutSec 30 `
                        -UseBasicParsing

                    if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) {
                        Write-Host "Health check passed: HTTP $($response.StatusCode)"
                    } else {
                        throw "Unexpected HTTP status: $($response.StatusCode)"
                    }
                } catch {
                    Write-Error "Health check failed: $_"
                    exit 1
                }
            '''
        }
    }

}
