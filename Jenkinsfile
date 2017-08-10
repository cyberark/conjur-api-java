#!/usr/bin/env groovy

pipeline {
  agent { label 'executor-v2' }

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  stages {
    stage('Create and archive the Maven package') {
      steps {
        echo 'TODO'
      }
    }
    stage('Run tests and archive test results') {
      steps {
        sh './test.sh'

        junit 'target/surefire-reports/*.xml'
      }
    }

    stage('Publish the Maven package') {
      when {
        branch 'master'
      }
      steps {
        echo 'TODO'
      }
    }
  }

  post {
    always {
      deleteDir()
    }
    failure {
      slackSend(color: 'danger', message: "${env.JOB_NAME} #${env.BUILD_NUMBER} FAILURE (<${env.BUILD_URL}|Open>)")
    }
    unstable {
      slackSend(color: 'warning', message: "${env.JOB_NAME} #${env.BUILD_NUMBER} UNSTABLE (<${env.BUILD_URL}|Open>)")
    }
  }
}
