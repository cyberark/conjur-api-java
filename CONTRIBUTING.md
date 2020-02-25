# Contributing to the Conjur API for Java

## Table of Contents
- [Prerequisites](#prerequisites)
- [Building and Testing](#building-and-testing)
- [Troubleshooting Steps](#troubleshooting-steps)
- [Contribution Workflow](#contribution-workflow)

## Prerequisites
### Java
To work in this codebase, you will need the latest version of the [Java JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) installed.

### Docker
This project is container-based and therefore, you will need [Docker](https://hub.docker.com/) in order to make and view your changes.

### Access to Conjur Registry (for contributing to testing ONLY)
To contribute to testing, you will need both the Conjur OSS and DAP (Enterprise). You shouldn't have a problem pulling down the OSS image, but to pull the Enterprise image, you must be granted access to the Conjur registry.

You will want to get in contact with the Infrastructure team via the [CyberArk Commons](https://discuss.cyberarkcommons.org/) to be granted access. If you have been granted access, run: `docker login registry.tld`.
 
_NOTE:_ Your password should be the API key given to you by the Infrastructure team.
 

## Building and Testing
Before making changes, it is recommended that the test script is run first to ensure that Java and the provided dependencies are not out-of-date.
You can run the test script in your shell like so:
`./test.sh`

_NOTE:_ The tests (for both Conjur OSS and DAP) are part of the build so if a change is made to the actual API, but the tests fail, the build will also fail.

## Troubleshooting Steps
This section includes helpful troubleshooting hints to help you navigate the codebase and resolve problems that you might be experiencing during development.

### Common Certificate-related Errors
- _org.apache.cxf.interceptor.Fault: Could not send Message_ 
- _sun.security.provider.certpath.SunCertPathBuilderException unable to find valid certification path to requested target_

#### Possible solutions
1. Check that the `$JAVA_HOME` path in the container matches the `JAVA_PATH` variable path in the test script. Otherwise, Java will think a new keystore is being created and will not (by default) look for the certificate in the path given.
2. Since we are using Java `cacerts` (Java-specific certificates), check that the fingerprints of the certificate in the test containers match the fingerprints in the appliance.
    
    To do so: 
    1. `docker exec` into the test container and run `keytool -list -keystore $JRE_HOME/lib/security/cacerts -alias <IMPORTED_CERT_ALIAS>`. The alias of the certificate can be found in the test script.
        1. If import was successful, the certificate fingerprint (SHA) should be returned.
    2. Navigate to `https://localhost:<PORT_OF_DAP/CONJUR_CONTAINER>` in your browser (make sure the DAP/Conjur container is still up and running). 
        1. You can find the exact ports by taking a look at each container's exposed ports in the `docker-compose.yml` file. 
        2. You can keep containers up by commenting out the `finish` function in the test script.
    3. For instructions on how to view certificates in browsers, take a look [here](https://www.globalsign.com/en/blog/how-to-view-ssl-certificate-details/).
    4. Check to see if the fingerprints of the host certificate (not the root certificate) match the one that was returned by the keytool.

3. Create a basic HTTPS client in Java to make the connection to the appliance. If you cannot make the connection (not getting data back), then you know there is a problem with the cacaerts collection (either on import or on creation)

### Jenkins Tests Error
- _The input device is not a TTY_

TTY ("teletype") is a terminal interface (from when terminals were attached to mainframes) that support input/output streams. Since then, pseudo-terminal drivers/emulators were developed to allow terminals to perform actions and send signals without the need to write to terminal directly. When pushing your changes, you may notice that you are receiving the above error because Jenkins doesn't support terminal connection sessions (with stdin and stdout streams) and therefore doesn't execute its jobs in a TTY. To disable this, add `-T` to your docker command.
    _NOTE_: this can happen with `cat` as it may sometimes not notice that the input stream has ended.

## Contribution Workflow
1. Fork or clone repository
2. If issue for relevant change has not been created, open one [here](https://github.com/cyberark/conjur-api-java/issues)
3. Add the `implementing` label to the issue once you begin to work on it 
4. Create your feature branch (`git checkout -b my-new-feature`)
5. Run test script (`./test.sh`) to ensure tests are not out of date
5. Once changes are made, run test script again to ensure your changes haven't broken anything
6. Commit your changes (`git commit -m 'Add X feature that does Y'`)
7. Push to the branch (`git push origin my-new-feature`)
8. Create new Pull Request, linking the issue in the description (e.g. `Connected to #123`) and ask another developer to review and merge your code
9. Replace `implementing` with `implemented` label in issue
