pipeline {
  agent {
    docker {
      image 'maven:3.8-eclipse-temurin-17'
      label 'docker'
    }
  }
  
  stages {
    stage ('Maven') {
      steps {
        withMaven(mavenSettingsConfig: 'mvn-elearn-repo-settings') {
          sh '$MVN_CMD clean spring-boot:build-image -Dspring-boot.build-image.publish=true'
        }
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
