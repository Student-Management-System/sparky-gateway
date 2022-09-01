
pipeline {
  agent any
  tools {
    maven 'maven-3.8.6' 
  }
  stages {
    stage ('Build') {
      steps {
        sh 'mvn clean package'
      }
      app = docker.build("sparky/gateway")
    }
  }
}
