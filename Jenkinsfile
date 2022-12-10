pipeline {
  agent {
    label 'maven && docker && jdk17'
  }
  
  environment {
    GITHUB_CREDS = credentials('github-ssejenkins')
    GITHUB_USER = '${GITHUB_CREDS_USR}'
    GITHUB_PASSWORD = '${GITHUB_CREDS_PSW}'
    
    DOCKER_GROUP = 'e-learning-by-sse'
    DOCKER_REGISTRY = 'ghcr.io'
    DOCKER_USER = '${GITHUB_CREDS_USR}'
    DOCKER_PASSWORD = '${GITHUB_CREDS_PSW}'
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
