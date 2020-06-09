#!/usr/bin/env groovy

pipeline {
  agent { label 'executor-v2' }

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  triggers {
    cron(getDailyCronString())
  }

  stages {
    stage('Validate') {
      parallel {
        stage('Changelog') {
          steps { sh './bin/parse-changelog.sh' }
        }
      }
    }

    stage('Create and archive the Maven package') {
      steps {
        sh './bin/build.sh'
      }
    }

    stage('Run tests and archive test results') {
      steps {
        lock("api-java-${env.NODE_NAME}") {
          sh './bin/test.sh'
        }

        junit 'target/surefire-reports/*.xml'
      }
    }

    stage('Perform Snapshot Deployment') {
      when {
        branch 'master'
      }
      steps {
        echo './bin/deploy-snapshot'
      }
    }

    stage('Perform Release Deployment') {
      when {
        buildingTag()
      }
      steps {
        echo './bin/deploy-release'
      }
    }
  }

  post {
    always {
      cleanupAndNotify(currentBuild.currentResult)
    }
  }
}