pipeline {
  agent any
  environment {
    DOCKER_TARGET = 'e-Learning-by-SSE/infrastructure-gateway'
    DOCKER_REGISTRY = 'https://ghcr.io'
    JENKINS_DOCKER_CREDS = 'github-ssejenkins'
  }
  
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
      steps {
        sh 'mvn clean package'
        script {
          app = docker.build("${DOCKER_TARGET}")
        }
        junit '**/target/surefire-reports/*.xml'
      }
    }

    stage ('Analysis') {
      steps {
        jacoco()
        archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
        script {
          def checkstyle = scanForIssues tool: [$class: 'CheckStyle'], pattern: '**/target/checkstyle-result.xml'
          publishIssues issues:[checkstyle]
        } 
      }
    }

    stage ('Publish') {
      when { 
        expression {
          currentBuild.result == null || currentBuild.result == 'SUCCESS' 
        }
      }
      steps {
        script {
          docker.withRegistry("${DOCKER_REGISTRY}", "${JENKINS_DOCKER_CREDS}") {
            app.push("${env.BUILD_NUMBER}")
            app.push("latest")
          }
        }
      }
    }

  }
}
