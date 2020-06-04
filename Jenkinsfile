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
          steps { sh './bin/parse-changelog' }
        }
      }
    }
    
    stage('Create and archive the Maven package') {
      steps {
        sh './bin/build'

    stage('Fetch tags') {
      steps {
        withCredentials(
          [usernameColonPassword(credentialsId: 'conjur-jenkins-api', variable: 'GITCREDS')]
        ) {
          sh '''
            git fetch --tags `git remote get-url origin | sed -e "s|https://|https://$GITCREDS@|"`
          '''
        }
      }
    }

    stage('Run tests and archive test results') {
      steps {
        lock("api-java-${env.NODE_NAME}") {
          sh './bin/test'
        }

        junit 'target/surefire-reports/*.xml'
      }
    }
    stage('Publish the Maven package') {
      when {
        branch 'master'
      }
      steps {
        sh './bin/publish'
      }
    }
  }

  post {
    always {
      cleanupAndNotify(currentBuild.currentResult)
    }
  }
}
