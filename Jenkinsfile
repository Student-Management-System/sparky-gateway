
pipeline {
  agent any
  tools {
    maven 'Maven 3.8.6' 
  }
  stages {
    stage ('Clone') {
      steps {
        checkout scm
      }
    }
    stage ('Build & Tests') {
      agent {
        dockerfile { 
          filename 'Dockerfile'
          dir 'test-bootstrap'
          label 'sparky-gateway-test-setup'
        }
      }
      steps {
        sh 'mvn clean package'
        script {
          app = docker.build("ssedevelopment/sparky-gateway")
        }
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }
    stage ('Publish') {
      steps {
        when { 
          expression {
            currentBuild.result == null || currentBuild.result == 'SUCCESS' 
          }
        }
        script {
          docker.withRegistry('https://ghcr.io', '2ad31065-44e1-4850-a3b1-548e17aa6757') {
            app.push("${env.BUILD_NUMBER}")
            app.push("latest")
          }
        }
      }
    }
  }
}
