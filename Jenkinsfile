pipeline {
  agent any
  environment {
    DOCKER_TARGET = 'e-Learning-by-SSE/infrastructure-gateway'
    DOCKER_REGISTRY = 'ghcr.io'
    JENKINS_DOCKER_CREDS = 'github-ssejenkins'
    
    GITHUB_CREDS = credentials('github-ssejenkins')
    GITHUB_USER = '$GITHUB_CREDS_USR'
    GITHUB_PASSWORD = '$GITHUB_CREDS_PSW'
  }
  
  tools {
    maven 'Maven 3.8.6' 
  }
  
  stages {

    stage ('Maven') {
      steps {
        sh 'mvn clean -s mvn-settings.xml spring-boot:build-image -Dspring-boot.build-image.publish=true'
        junit '**/target/surefire-reports/*.xml'
        archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
      }
    }

    stage ('Analysis') {
      steps {
        jacoco()
        script {
          def checkstyle = scanForIssues tool: [$class: 'CheckStyle'], pattern: '**/target/checkstyle-result.xml'
          publishIssues issues:[checkstyle]
        } 
      }
    }
  }
}
