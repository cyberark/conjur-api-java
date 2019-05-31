# Contributing to the Conjur API for Java

## Table of Contents
- [Prerequisities](#prerequisites)
- [Building and Testing](#building-and-testing)
- [Troubleshooting Steps](#troubleshooting-steps)
- [Style Guide](#style-guide)

## Prerequisites
### Java
To work in this codebase, you will want to need the latest version of the Java JDK installed. Dependencies for this project will be installed locally when the project is built.

### Docker
This project is container-based and therefore, you will need [Docker](https://docs.docker.com/v17.12/) in order to make and view your changes.

### Access to Conjur Registry
This project requires both OSS and Enterprise images. You shouldn't have a problem pulling down the OSS image, but to pull the Enterprise image, you must be granted access to the Conjur registry. 

You will want to get in contact with the Infrastructure team via our Slack to be granted access. If you have been granted access, run: `docker login registry2.itci.conjur.net` 
 
_NOTE:_ Your password should be the API key given to you by the Infrastructure team.
 

## Building and Testing
Before making changes, it is recommended that the test script is run first to ensure that Java and the provided dependencies are not out-of-date.
You can run the test script in your shell like so:
`./test.sh`

_NOTE:_ The tests (for both OSS and Enterprise) are part of the build so if a change is made to the actual API, but the tests fail, the build will also fail.

## Troubleshooting Steps
This section includes helpful hints for troubleshooting to help you navigate the codebase and resolve the problems that you might be experiencing during development.

### Common Certificate-related Errors
- _org.apache.cxf.interceptor.Fault: Could not send Message_ 
- _sun.security.provider.certpath.SunCertPathBuilderException unable to find valid certification path to requested target_

#### Possible solutions
1. Check that the environment variable path of `$JAVA_HOME` in the container matches the `JAVA_PATH` variable path in the test script, otherwise Java will think a new keystore is being created and will not (by default) look for the certificate in the path given.
2. Since we are using Java cacerts, check that the fingerprints of the certificate in the test containers match the fingerprints of the appliance. 
    
    To do so: 
    1. Drop down into the test container and run `keytool -list -keystore $JRE_HOME/lib/security/cacerts -alias <IMPORTED_CERT_ALIAS>`. The alias of the certificate can be found in the test script.
        1. If import was done correctly, the certificate fingerprint (SHA) should be returned.
         
    2. Navigate to `https://localhost:<PORT_OF_DAP/CONJUR_CONTAINER>` in your browser (make sure the DAP/Conjur to still up by running `docker ps`). 
        1. You can find the exact ports by taking a look at each container's exposed ports in the `docker-compose.yml` file. 
        2. Keep containers up by commenting out the finish function in the test script.
    3. For instructions on how to view certificates in browsers, take a look [here](https://www.globalsign.com/en/blog/how-to-view-ssl-certificate-details/).
    4. Check to see if the fingerprints of the host certificate (not the root certificate) match the one that was returned by the keytool.

3. Create a basic HTTPS client in Java to make the connection to the appliance. If you cannot make the connection (not getting data back), then you know there is a problem with the cacaerts collection (either import or creation)

### Jenkins tests
- _The input device is not a TTY_

When pushing your changes, you may notice that you are receiving the above error. This error means that Jenkins expects text input/output via the shell. To disable this, add `-T` to your docker command.    
    _NOTE_: this can happen with `cat` as it may sometimes not notice that the input stream has ended.
    
## Style Guide
Use this guide to maintain consistent style across the Conjur API for Java project.
