#!/usr/bin/env groovy
@Library([
  "product-pipelines-shared-library",
  "conjur-enterprise-sharedlib"
]) _

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
      summon -e release ./bin/publish.sh
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
  agent { label 'conjur-enterprise-AmznDocker' }

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

  parameters {
    booleanParam(name: 'RUN_AZURE_TESTS', defaultValue: false, description: 'Run Azure tests')
    booleanParam(name: 'RUN_GCP_TESTS', defaultValue: false, description: 'Run GCP tests')
    booleanParam(name: 'RUN_AWS_TESTS', defaultValue: false, description: 'Run AWS IAM tests')
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

    stage('Get Cloud Test Agents') {
      steps {
        script {
          // Azure and GCP have no AmznDocker equivalent, so those pools are
          // retained. AWS tests run directly on this AmznDocker agent (which
          // itself runs in AWS with an instance profile) — no separate
          // ExecutorV2 pool is requested for them.
          if (params.RUN_AZURE_TESTS) {
            INFRAPOOL_AZURE_EXECUTORV2_AGENTS = getInfraPoolAgent(type: "AzureExecutorV2", quantity: 1, duration: 1)
            INFRAPOOL_AZURE_EXECUTORV2_AGENT_0 = INFRAPOOL_AZURE_EXECUTORV2_AGENTS[0]
            azureInfrapool = infraPoolConnect(INFRAPOOL_AZURE_EXECUTORV2_AGENT_0, {})
          }

          if (params.RUN_GCP_TESTS) {
            INFRAPOOL_GCP_EXECUTORV2_AGENTS = getInfraPoolAgent(type: "GcpExecutorV2", quantity: 1, duration: 1)
            INFRAPOOL_GCP_EXECUTORV2_AGENT_0 = INFRAPOOL_GCP_EXECUTORV2_AGENTS[0]
            gcpInfrapool = infraPoolConnect(INFRAPOOL_GCP_EXECUTORV2_AGENT_0, {})
          }
        }
      }
    }

    stage('Mark Workspace as Safe Git Directory') {
      steps {
        script {
          sh 'git config --global --add safe.directory $WORKSPACE'
        }
      }
    }

    stage('Validate Changelog') {
      steps {
        parseChangelog()
      }
    }

    // Generates a VERSION file based on the current build number and latest version in CHANGELOG.md
    stage('Validate Changelog and set version') {
      steps {
        script {
          updateVersion("CHANGELOG.md", "${BUILD_NUMBER}")
          sh '''
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
          sh './bin/build-tools-image.sh'

          // Run Docker Image to compile code and build jar
          sh './bin/build-package.sh'
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
          sh './bin/test.sh'
        }
      }
    }

    stage('Run tests and archive results (JDK25)') {
      environment {
        INFRAPOOL_REGISTRY_URL = "registry.tld"
        INFRAPOOL_JDK_VERSION = "25"
      }
      steps {
        script {
          lock("api-java-${env.NODE_NAME}") {
            sh './bin/test.sh'

            // build-package.sh and test.sh both run mvn as root inside the
            // tools/test images (the /root/.m2 cache mount depends on it),
            // so target/ is root-owned. Build and test now share one
            // workspace on AmznDocker, so fix ownership before stash/unstash
            // touch these same paths again as the build user.
            sh 'sudo chown -R "$(id -u):$(id -g)" target'

            stash name: 'jacoco', includes: 'target/site/jacoco/jacoco.xml'
            unstash 'jacoco'
            codacy action: 'reportCoverage', filePath: "target/site/jacoco/jacoco.xml"

            stash includes: 'target/surefire-reports/*.xml', name: "test-results"
            unstash 'test-results'
          }
        }
        junit 'target/surefire-reports/*.xml'
      }
    }

    stage('Run Azure tests') {
      when {
        expression { params.RUN_AZURE_TESTS }
      }
      steps {
        script {
          azureInfrapool.agentSh '''
            set +e
            export RUN_AZURE_TESTS=true
            export TEST_FILTER="AzureAuthenticatorIntegrationTests"
            summon -e azure ./bin/test.sh
            rc=$?
            exit $rc
          '''
        }
      }
    }

    stage('Run GCP tests') {
      when {
        expression { params.RUN_GCP_TESTS }
      }
      steps {
        script {
          // Fetch token from GCP metadata on the GCP agent
          gcpInfrapool.agentSh './ci/get_gcp_token.sh "data/test/gcp-apps/test-app" "conjur" gcp-ctx'
          GCP_ID_TOKEN = gcpInfrapool.agentSh(script: 'cat gcp-ctx/token', returnStdout: true).trim()
          GCP_PROJECT_ID = gcpInfrapool.agentSh(script: 'cat gcp-ctx/project-id', returnStdout: true).trim()

          // Run tests on this AmznDocker agent (which has Docker)
          sh """
            set +e
            export RUN_GCP_TESTS=true
            export TEST_FILTER="GCPAuthenticatorIntegrationTests"
            export GCP_ID_TOKEN="${GCP_ID_TOKEN}"
            export GCP_PROJECT_ID="${GCP_PROJECT_ID}"
            ./bin/test.sh
            rc=\$?
            exit \$rc
          """
        }
      }
    }

    stage('Run AWS tests') {
      when {
        expression { params.RUN_AWS_TESTS }
      }
      steps {
        script {
          // Fetch AWS identity from this AmznDocker agent's own instance profile
          def callerIdentityJson = sh(script: 'aws sts get-caller-identity', returnStdout: true).trim()
          // JSON: {"UserId":"...","Account":"123456789012","Arn":"arn:aws:sts::123456789012:assumed-role/RoleName/session"}
          def awsAccountId = (callerIdentityJson =~ /"Account"\s*:\s*"([^"]+)"/)[0][1]
          def awsArn      = (callerIdentityJson =~ /"Arn"\s*:\s*"([^"]+)"/)[0][1]
          def awsRoleName = awsArn.tokenize('/')[1]  // arn:.../assumed-role/RoleName/session → index 1

          sh """
            set +e
            export RUN_AWS_TESTS=true
            export TEST_FILTER="AWSIAMAuthenticatorIntegrationTests"
            export AWS_ACCOUNT_ID="${awsAccountId}"
            export AWS_ROLE_NAME="${awsRoleName}"
            ./bin/test.sh
            rc=\$?
            exit \$rc
          """
        }
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
          sh 'cp VERSION.original VERSION'
          release { billOfMaterialsDirectory, assetDirectory ->
            // Publish release artifacts to all the appropriate locations
            // Copy any artifacts to assetDirectory to attach them to the Github release
            sh "ASSET_DIR=\"${assetDirectory}\" summon -e release ./bin/publish.sh"
          }
        }
      }
    }
  }

  post {
    always {
      releaseInfraPoolAgent(".infrapool/release_agents")
      // Resolve ownership issue before running infra post hook
      sh 'git config --global --add safe.directory ${PWD}'
      infraPostHook()
    }
  }
}
