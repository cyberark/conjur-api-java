#!/usr/bin/env groovy
@Library("product-pipelines-shared-library") _

// Automated release, promotion and dependencies
properties([
  // Include the automated release parameters for the build
  release.addParams(),
  // Dependencies of the project that should trigger builds
  dependencies([])
])

// Performs release promotion.  No other stages will be run
if (params.MODE == "PROMOTE") {
  release.promote(params.VERSION_TO_PROMOTE) { infrapool, sourceVersion, targetVersion, assetDirectory ->
    // Any assets from sourceVersion Github release are available in assetDirectory
    // Any version number updates from sourceVersion to targetVersion occur here
    // Any publishing of targetVersion artifacts occur here
    // Anything added to assetDirectory will be attached to the Github Release

    // Pass assetDirectory through to publish.sh as an env var.
    env.ASSET_DIR=assetDirectory

    infrapool.agentSh """
      export ASSET_DIR="${env.ASSET_DIR}"
      export MODE="${params.MODE}"
      git checkout "v${sourceVersion}"
      echo -n "${targetVersion}" > VERSION
      cp VERSION VERSION.original
      ./bin/build-tools-image.sh
      ./bin/build-package.sh
      summon ./bin/publish.sh
      cp target/*.jar "${assetDirectory}"
    """

    // Ensure the working directory is a safe git directory for the subsequent
    // promotion operations after this block.
    infrapool.agentSh 'git config --global --add safe.directory "$(pwd)"'
  }

  // Copy Github Enterprise release to Github
  release.copyEnterpriseRelease(params.VERSION_TO_PROMOTE)

  return
}

pipeline {
  agent { label 'conjur-enterprise-common-agent' }

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  environment {
    // Sets the MODE to the specified or autocalculated value as appropriate
    MODE = release.canonicalizeMode()
  }

  triggers {
    cron(getDailyCronString())
    parameterizedCron(getWeeklyCronString("H(1-5)","%MODE=RELEASE"))
  }
  
  stages {
    // Aborts any builds triggered by another project that wouldn't include any changes
    stage ("Skip build if triggering job didn't create a release") {
      when {
        expression {
          MODE == "SKIP"
        }
      }
      steps {
        script {
          currentBuild.result = 'ABORTED'
          error("Aborting build because this build was triggered from upstream, but no release was built")
        }
      }
    }
    
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

    // Generates a VERSION file based on the current build number and latest version in CHANGELOG.md
    stage('Validate Changelog and set version') {
      steps {
        script {
          updateVersion(INFRAPOOL_EXECUTORV2_AGENT_0, "CHANGELOG.md", "${BUILD_NUMBER}")
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh '''
            cp VERSION VERSION.original
            version="$(<VERSION)"
            echo "Current VERSION content: ${version}"
            echo "${version}-SNAPSHOT" > VERSION
            cp VERSION VERSION.snapshot
          '''
        }
      }
    }

    stage('Build') {
      steps {
        script {
          // Build Docker Image for tools (eg mvn)
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './bin/build-tools-image.sh'

          // Run Docker Image to compile code and build jar
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './bin/build-package.sh'
        }
      }
    }

    stage('Run tests (JDK8)') {
      environment {
        INFRAPOOL_REGISTRY_URL = "registry.tld"
        INFRAPOOL_JDK_VERSION = "8"
      }
      steps {
        script {
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './bin/test.sh'
        }
      }
    }

    stage('Run tests and archive results (JDK23)') {
      environment {
        INFRAPOOL_REGISTRY_URL = "registry.tld"
        INFRAPOOL_JDK_VERSION = "23"
      }
      steps {
        script {
          lock("api-java-${env.NODE_NAME}") {
            INFRAPOOL_EXECUTORV2_AGENT_0.agentSh './bin/test.sh'
            INFRAPOOL_EXECUTORV2_AGENT_0.agentStash includes: 'target/surefire-reports/*.xml', name: "test-results"
            unstash 'test-results'
          }
        }
        junit 'target/surefire-reports/*.xml'
      }
    }

    stage('Release') {
      when {
        expression {
          MODE == "RELEASE"
        }
      }
      steps {
        script {
          INFRAPOOL_EXECUTORV2_AGENT_0.agentSh 'cp VERSION.original VERSION'
          release(INFRAPOOL_EXECUTORV2_AGENT_0) { billOfMaterialsDirectory, assetDirectory ->
            // Publish release artifacts to all the appropriate locations
            // Copy any artifacts to assetDirectory to attach them to the Github release
            INFRAPOOL_EXECUTORV2_AGENT_0.agentSh "ASSET_DIR=\"${assetDirectory}\" summon ./bin/publish.sh"
          }
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
