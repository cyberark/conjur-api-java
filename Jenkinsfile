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
        echo 'TODO'
      }
    }

    stage('Run tests and archive test results') {
      steps {
        lock("api-java-${env.NODE_NAME}") {
          sh './test.sh'
        }

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
      cleanupAndNotify(currentBuild.currentResult)
    }
  }
}
