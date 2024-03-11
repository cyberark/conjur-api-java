#!/usr/bin/env groovy

pipeline {
  agent { label 'conjur-enterprise-common-agent' }

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  triggers {
    cron(getDailyCronString())
  }

  stages {
    stage('Scan for internal URLs') {
      steps {
        script {
          detectInternalUrls()
        }
      }
    }

    stage('Get InfraPool Agent') {
      steps {
        script {
          INFRAPOOL_EXECUTORV2_AGENT_0 = getInfraPoolAgent.connected(type: "ExecutorV2", quantity: 1, duration: 1)[0]
        }
      }
    }

    stage('Validate Changelog') {
      steps {
        parseChangelog(INFRAPOOL_EXECUTORV2_AGENT_0)
      }
    }

    stage('Create and archive the Maven package') {
      steps {
        script {
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './bin/build.sh'
        }
      }
    }

    stage('Run tests and archive test results') {
      steps {
        script {
          lock("api-java-${env.NODE_NAME}") {
            INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './bin/test.sh'
            INFRAPOOL_EXECUTORV2_AGENT_0.agentStash includes: 'target/surefire-reports/*.xml', name: 'test-results'
            unstash 'test-results'
          }
        }

        junit 'target/surefire-reports/*.xml'
      }
    }

    stage('Perform Snapshot Deployment') {
      when {
        branch 'main'
      }
      steps {
        script {
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './bin/deploy-snapshot.sh'
        }
      }
    }

    stage('Perform Release Deployment') {
      when {
        buildingTag()
      }
      steps {
        script {
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './bin/deploy-release.sh'
        }
      }
    }
  }

  post {
    always {
      releaseInfraPoolAgent(".infrapool/release_agents")
    }
  }
}
