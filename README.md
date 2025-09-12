Secrets Manager API for Java
===================
Programmatic Java access to the Secrets Manager API (for both Conjur OSS and Secrets Manager, Self-Hosted).
This Java SDK allows developers to build new apps in Java that communicate with Conjur by
invoking our Secrets Manager API to perform operations on stored data (add, retrieve, etc).

## Table of Contents

- [Prerequisites](#prerequisites)
  * [Using conjur-api-java with Conjur Open Source](#using-conjur-api-java-with-conjur-open-source)
- [Setup](#setup)
  * [Using the Source Code](#using-the-source-code)
  * [Using the Jarfile](#using-the-jarfile)
  * [Using Maven Releases](#using-maven-releases)
  * [Using Maven Snapshots](#using-maven-snapshots)
  * [Using Other Dependency Management Configurations](#using-other-dependency-management-configurations)
- [Configuration](#configuration)
  * [Environment Variables](#environment-variables)
  * [System Properties](#system-properties)
- [Set Up Trust Between App and Secrets Manager](#set-up-trust-between-app-and-secrets-manager)
  * [Client-level trust](#client-level-trust)
  * [JVM-level trust](#jvm-level-trust)
- [Authorization Examples](#authorization-examples)
  * [Environment Variables](#environment-variables-1)
  * [System Properties](#system-properties-1)
  * [System Properties with Maven](#system-properties-with-maven)
  * [Username and Password](#username-and-password)
  * [Credentials](#credentials)
  * [Authorization Token](#authorization-token)
- [Client APIs](#client-apis)
  * [Secrets Manager Client Instance (`com.cyberark.conjur.api.Conjur`)](#secrets-manager-client-instance-comcyberarkconjurapiconjur)
  * [Variables (`client.variables()`)](#variables-clientvariables)
    + [`void addSecret(String variableId, String secret)`](#void-addsecretstring-variableid-string-secret)
    + [`String retrieveSecret(String variableId)`](#string-retrievesecretstring-variableid)
- [Jakarta REST JAX-RS Implementations](#jakarta-rest-jax-rs-implementations)
- [Troubleshooting](#troubleshooting)
  * [`error: package com.cyberark.conjur does not exist`](#error-package-comcyberarkconjur-does-not-exist)
  * [`java.lang.NoClassDefFoundError: javax/xml/bind/JAXBException`](#javalangnoclassdeffounderror-javaxxmlbindjaxbexception)
  * [SSL/TLS/Certificate Issues](#ssltlscertificate-issues)
- [Contributing](#contributing)
- [License](#license)

<!-- 
[Table of contents generated with markdown-toci](http://ecotrust-canada.github.io/markdown-toc/)
-->

## Prerequisites

It is assumed that Conjur OSS or Secrets Manager, Self-Hosted and the Secrets Manager CLI have already been
installed in the environment and running in the background. If you haven't done so,
follow these instructions for installation of the [OSS](https://docs.cyberark.com/conjur-open-source/latest/en/content/hometileslps/lp-tile2.htm?tocpath=Setup%7C_____0)
and these for installation of [Secrets Manager, Self-Hosted](https://docs.cyberark.com/Product-Doc/OnlineHelp/AAM-DAP/Latest/en/Content/HomeTilesLPs/LP-Tile2.htm).

Once Secrets Manager and the Secrets Manager CLI are running in the background, you are ready to start
setting up your Java app to work with our Secrets Manager Java API!

### Using conjur-api-java with Conjur Open Source 

Are you using this project with [Conjur Open Source](https://github.com/cyberark/conjur)? Then we 
**strongly** recommend choosing the version of this project to use from the latest [Conjur OSS 
suite release](https://docs.conjur.org/Latest/en/Content/Overview/Conjur-OSS-Suite-Overview.html). 
Conjur maintainers perform additional testing on the suite release versions to ensure 
compatibility. When possible, upgrade your Conjur version to match the 
[latest suite release](https://docs.conjur.org/Latest/en/Content/ReleaseNotes/ConjurOSS-suite-RN.htm); 
when using integrations, choose the latest suite release that matches your Conjur version. For any 
questions, please contact us on [Discourse](https://discuss.cyberarkcommons.org/c/conjur/5).

## Setup
The Secrets Manager Java API can be imported manually through building the source code locally, 
or by using a dependency configuration to import from Maven Central. Please refer to
the following instructions for your specific use case.

### Using the Source Code

You can grab the library's dependencies from the source by using Maven **or** locally
by generating a JAR file and adding it to the project manually.

To do so from the source using Maven, following the setup steps below:

1. Create new Maven project using an IDE of your choice
2. If you are using Maven to manage your project's dependencies, include the following
   Secrets Manager API dependency snippet in your `pom.xml` under `<project>`/`<dependencies>`:

```xml
    <dependency>
      <groupId>com.cyberark.conjur.api</groupId>
      <artifactId>conjur-api</artifactId>
      <version>3.0.5</version>
    </dependency>
```

_NOTE:_ Depending on what version of the Java compiler you have, you may need to update
the version. At this time, the `{version}` that we are targeting compatibility with is
Java 8:

```xml
  <properties>
    <maven.compiler.source>{version}</maven.compiler.source>
    <maven.compiler.target>{version}</maven.compiler.target>
  </properties>
```

3. Run `mvn install -DskipTests` in this repo's directory to install Secrets Manager API into your
   local maven repository.

### Using the Jarfile

If generating a JAR is preferred, you can build the library locally and add the dependency
to the project manually by following the setup steps below:

1. Clone the Secrets Manager Java API repo locally: `git clone {repo}`
2. Go into the cloned repository with `cd conjur-api-java`
3. Run `mvn package -DskipTests` to generate a JAR file. The output `.jar` files will be located
   in the `target` directory of the repo

_NOTE:_ The above command runs `mvn package` without running the integration tests, since
these require access to a Secrets Manager instance. You can run the integration tests with mvn package
once you finish the configuration. For more information on how to run the tests, take a look at
our [Contributing](https://github.com/cyberark/conjur-api-java/blob/main/CONTRIBUTING.md) guide.

4a. For Intellij, Follow the steps outlined [here](https://www.jetbrains.com/help/idea/library.html)
    to add the SDK JAR files into the new app's project.
4b. For Eclipse you `Right click project > Build Path > Configure Build Path > Library > Add External JARs`.
4c. If you are working with the Maven CLI, you can manually install the `.jar` into your Maven.
    repository by running the following (replacing `$VERSION` with the appropriate version
    of the API):
    ```sh-session
    $ mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file \
        -Dfile=/path/to/api/repo/target/conjur-api-$VERSION.jar
    ```
    or
    ```sh-session
    $ mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file \
        -Dfile=/path/to/api/repo/target/conjur-api-$VERSION-with-dependencies.jar
    ```
    or
    ```sh-session
    $ mvn install:install-file -Dfile=/path/to/api/repo/target/conjur-api-$VERSION-with-dependencies.jar \
        -DgroupId=com.cyberark.conjur.api \
        -DartifactId=conjur-api \
        -Dversion=$VERSION \
        -Dpackaging=jar
    ```

### Using Maven Releases

To make use of tagged releases published to Maven, verify that you have the dependency 
added to your `pom.xml`

1. Add the following snippet to `pom.xml`
```xml
<dependency>
  <groupId>com.cyberark.conjur.api</groupId>
  <artifactId>conjur-java-api</artifactId>
  <version>x.x.x</version>
</dependency>
```

### Using Maven Snapshots
To make use of SNAPSHOTS, which are deployed following a nightly build, there are 
several steps required for configuring your project.

> Note: Snapshots contain the latest changes to `conjur-java-api`, but it is recommended
> to use the current stable release unless there is a significant update required by your
> project 

1. Add the following to your `settings.xml`
```xml
<profiles>
  <profile>
     <id>allow-snapshots</id>
        <activation><activeByDefault>true</activeByDefault></activation>
     <repositories>
       <repository>
         <id>snapshots-repo</id>
         <url>https://oss.sonatype.org/content/repositories/snapshots</url>
         <releases><enabled>false</enabled></releases>
         <snapshots><enabled>true</enabled></snapshots>
       </repository>
     </repositories>
   </profile>
</profiles>
```

Alternatively, add the following to your list of repositories in `pom.xml`
```xml
<repository>
  <id>oss.sonatype.org-snapshot</id>
  <url>http://oss.sonatype.org/content/repositories/snapshots</url>
  <releases>
    <enabled>false</enabled>
  </releases>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
</repository>
```

2. In your `pom.xml`, verify that your `conjur-java-api` dependency includes `SNAPSHOT`
in the version tag.
```xml
<dependency>
  <groupId>com.cyberark.conjur.api</groupId>
  <artifactId>conjur-java-api</artifactId>
  <version>x.x.x-SNAPSHOT</version>
</dependency>
```

### Using Other Dependency Management Configurations
Please refer to the instructions available on [Maven Central](https://search.maven.org/artifact/com.cyberark.conjur.api/conjur-api) 
and select a version for specific instructions on including the Secrets Manager Java API in your
project through Gradle, Kotlin, and more!

## Configuration

Once the setup steps have been successfully run, we will now define the variables needed
to make the connection between the new app and Secrets Manager. You can do this by setting
[environment variables](#environment-variables), [system properties](#system-properties),
or some combination of both.

_NOTE:_ System properties will override enviroment values when both are defined for a
variable.

### Environment Variables

In Secrets Manager and Conjur OSS, environment variables are mapped to configuration variables
by prepending `CONJUR_` to the all-caps name of the configuration variable. For example,
`appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT` etc.

The following environment variables need to be included in the app's runtime environment in
order use the Secrets Manager API if no other configuration is done (e.g. over system properties or
CLI parameters):

- `CONJUR_APPLIANCE_URL` - The URL of the Secrets Manager instance you are connecting to. When connecting to
  Secrets Manager, Self-Hosted configured for high availability, this should be the URL of the master load balancer (if
  performing read and write operations) or the URL of a follower load balancer (if performing
  read-only operations).
- `CONJUR_ACCOUNT` - Secrets Manager account that you are connecting to. This value is set during Secrets Manager deployment.
- `CONJUR_AUTHN_LOGIN` - User/host identity
- `CONJUR_AUTHN_API_KEY` - User/host API key (or password; see notes on `CONJUR_AUTHN_URL`)
- `CONJUR_AUTHN_URL` - (optional) Alternate authentication endpoint. By default the client
  uses the standard `<applianceUrl>/authn` for generic username and API key login flow.

_Note:_ **If you use the default `CONJUR_AUTHN_URL` value or your `CONJUR_AUTHN_URL` ends with `/authn`,
the `CONJUR_AUTHN_API_KEY` is treated as a password otherwise `CONJUR_AUTHN_API_KEY` is treated as
an API key.**

For example, you can specify the environment variables like so:

```sh-session
export CONJUR_APPLIANCE_URL=https://conjur.myorg.com/api
export CONJUR_ACCOUNT=myorg
export CONJUR_AUTHN_LOGIN=host/myhost.example.com
export CONJUR_AUTHN_API_KEY=sb0ncv1yj9c4w2e9pb1a2s
```

or you could provide these at runtime to your jar:
```sh-session
$ CONJUR_APPLIANCE_URL=https://conjur.myorg.com/api \
  CONJUR_ACCOUNT=myorg \
  CONJUR_AUTHN_LOGIN=host/myhost.example.com \
  CONJUR_AUTHN_API_KEY=sb0ncv1yj9c4w2e9pb1a2s \
  java -jar myConjurClient.jar
```
If you are using a host-based user like this example shows, you will need to add the host to Secrets Manager with the proper privileges in policy in order to know the appropriate
`CONJUR_AUTHN_LOGIN` and `CONJUR_AUTHN_API_KEY` values.

### System Properties

This API can also be configured using [Java system properties](https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html)
You can specify any portion (or all) of the configuration values this way. The advantage
of this approach is that the values can be changed dynamically as needed. For example,
this snippet would let your client be able to use the API methods using properties defined
from the CLI:
```sh-session
java -jar myConjurClient.jar \
     -DCONJUR_APPLIANCE_URL=https://conjur.myorg.com/api \
     -DCONJUR_ACCOUNT=myorg \
     -DCONJUR_AUTHN_LOGIN=host/myhost.example.com \
     -DCONJUR_AUTHN_API_KEY=sb0ncv1yj9c4w2e9pb1a2s
```

If you are using Maven, you can also specify these proprerties on the CLI:

```sh-session
mvn exec:java \
     -DCONJUR_APPLIANCE_URL=https://conjur.myorg.com/api \
     -DCONJUR_ACCOUNT=myorg \
     -DCONJUR_AUTHN_LOGIN=host/myhost.example.com \
     -DCONJUR_AUTHN_API_KEY=sb0ncv1yj9c4w2e9pb1a2s \
     -Dexec.mainClass="com.myorg.client.App"
```

_NOTE:_ When using properties to configure Secrets Manager APIs, be careful not to persist sensitive
values (like the API key) in source-controlled property files!

## Set Up Trust Between App and Secrets Manager

By default, the Secrets Manager appliance generates and uses self-signed SSL certificates. Without
trusting them, your Java app will not be able to connect to the Secrets Manager server over APIs
and so you will need to configure your app to trust them. You can accomplish this by using
the [Client-level `SSLContext`](#client--level-trust) when creating the client or with a
[JVM-level trust](#jvm--level-trust) by loading the Secrets Manager certificate into Java's CA
keystore that holds the list of all the allowed certificates for https connections.

### Client-level trust

We can set up a trust between the client application and a Secrets Manager server using
Java `javax.net.ssl.SSLContext`. This can be done from Java code during
Secrets Manager class initialization.

Usable in Kubernetes/OpenShift environment to setup TLS trust with Secrets Manager
server dynamically from the Kubernetes secret and/or configmap data.

```java
final String conjurTlsCaPath = "/var/conjur-config/tls-ca.pem";

final CertificateFactory cf = CertificateFactory.getInstance("X.509");
final FileInputStream certIs = new FileInputStream(conjurTlsCaPath);
final Certificate cert = cf.generateCertificate(certIs);

final KeyStore ks = KeyStore.getInstance("JKS");
ks.load(null);
ks.setCertificateEntry("conjurTlsCaPath", cert);

final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
tmf.init(ks);

SSLContext conjurSSLContext = SSLContext.getInstance("TLS");
conjurSSLContext.init(null, tmf.getTrustManagers(), null);
```

### JVM-level trust

For a JVM-level trust between Secrets Manager and the API client, you need to load the Secrets Manager
certificate into Java's CA keystore that holds the list of all the allowed certificates
for https connections.

First, we need to get a copy of this certificate, which you can get using `openssl`. Run the
following step from a terminal with OpenSSL that has access to Secrets Manager:

```sh-session
$ openssl s_client -showcerts -servername myconjurserver.com \
    -connect myconjusrserver.com:443 < /dev/null 2> /dev/null \
    | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > conjur.pem

$ # Check that the certificate was properly retrieved. If you do not see this kind of output
$ # ensure that you are providing OpenSSL the correct server information
$ cat conjur.pem
-----BEGIN CERTIFICATE-----
...
-----END CERTIFICATE-----
```
This will save the certificate chain to a file called 'conjur.pem'. Since Java doesn't work
natively with the `pem` certificate encoding format, you'll need to convert it to the `der`
format:

```sh-session
$ openssl x509 -outform der -in conjur.pem -out conjur-default.der
```

Next, you'll need to locate the path to the JRE from the process environment running the Java
app. In the case of Java 8 on most standard Linux distributions it's
`/usr/lib/jvm/java-8-openjdk-amd64/jre`. We will export this path to `$JRE_HOME` for convenience.
If the file `$JRE_HOME/lib/security/cacerts` doesn't exist (you might need to be root to see it),
double check that the `JRE_HOME` path is correct. Once you've found it, you can add the
appliance's cert to Java's certificate authority keystore like this:

```sh-session
$ sudo -E keytool -importcert \
    -alias conjur-default \
    -keystore "$JRE_HOME/lib/security/cacerts" \
    -storepass changeit \
    -file ./conjur-default.der

Owner: CN=myconjurserver.com
Issuer: CN=myconjurserver.com, OU=Conjur CA, O=myorg
Serial number: 9e930ced498d74b4faf98e6d4f9d90ebdebebd57
Valid from: Mon Mar 30 16:51:15 CDT 2020 until: Thu Mar 28 16:51:15 CDT 2030
Certificate fingerprints:
         SHA1: 7A:A3:78:22:50:03:52:C2:B5:3E:1D:98:48:26:82:71:18:FB:2E:26
         SHA256: ED:77:BA:4A:81:EB:6C:26:E9:82:AC:75:51:99:9A:2F:76:D5:3C:A2:B4:8D:5D:87:EB:A6:01:49:FC:2F:28:FF
...
Trust this certificate? [no]:  yes
Certificate was added to keystore

$ # Make sure you do not see `keytool error: java.io.FileNotFoundException` error. If you do,
$ # your addition of the cert did not work.
```

_Note:_ On macOS, your default Java may not be able to run this tool so you may need to install
an alternate JDK like `openjdk`. You can find more info about this [here](https://docs.oracle.com/javase/8/docs/technotes/guides/install/mac_jdk.html)
and [here](https://formulae.brew.sh/formula/openjdk).

Verify the addition of the SSL key:
```sh-session
$ sudo -E keytool -list \
    -storepass changeit \
    -keystore $JAVA_HOME/lib/security/cacerts | grep conjur
conjur-default, May 6, 2020, trustedCertEntry,
```

There you have it! Now you are all configured to start leveraging the Secrets Manager Java API in
your Java program.

## Authorization Examples

As mentioned in the [Configuration](#configuration) section, you can provide varying ways
for your app to authenticate against a Secrets Manager server. Generally environment variables are
most common but this isn't the only way. In addition to explicitly setting these environment
variables, you can do so by providing [properties](#system-properties), using the Credentials
object, or by providing an Authorization Token. Once you have chosen from one of the
patterns below that works for you, you can now create a `Conjur` class instance values to
access Secrets Manager services and make RESTful API calls.

_Note:_ **As mentioned before, if you use the default `CONJUR_AUTHN_URL` value or your
`CONJUR_AUTHN_URL` ends with `/authn`, the `CONJUR_AUTHN_API_KEY` is treated as a password
otherwise `CONJUR_AUTHN_API_KEY` is treated as an API key.**

### Environment Variables

```bash
export CONJUR_ACCOUNT=<account specified during Secrets Manager setup>
export CONJUR_APPLIANCE_URL=<Secrets Manager endpoint URL>
export CONJUR_AUTHN_LOGIN=<user/host identity>
export CONJUR_AUTHN_API_KEY=<user/host API key or password - see notes about `CONJUR_AUTHN_URL`>
```
```java
import com.cyberark.conjur.api.Conjur;

// Configured using environment variables
Conjur conjur = new Conjur();
// or using custom SSLContext setup as conjurSSLContext variable
Conjur conjur = new Conjur(conjurSSLContext);
```

### System Properties

```sh-session
$ java -jar myConjurClient.jar \
     -DCONJUR_ACCOUNT=<account specified during Secrets Manager setup> \
     -DCONJUR_APPLIANCE_URL=<Secrets Manager endpoint URL> \
     -DCONJUR_AUTHN_LOGIN=<user/host identity> \
     -DCONJUR_AUTHN_API_KEY=<user/host API key - see notes about `CONJUR_AUTHN_URL`>
```
```java
import com.cyberark.conjur.api.Conjur;

// Configured using system properties
Conjur conjur = new Conjur();
// or using custom SSLContext setup as conjurSSLContext variable
Conjur conjur = new Conjur(conjurSSLContext);
```

### System Properties with Maven

```sh-session
$ mvn exec:java \
  -DCONJUR_ACCOUNT=<account specified during Secrets Manager setup> \
  -DCONJUR_APPLIANCE_URL=<Secrets Manager endpoint URL> \
  -DCONJUR_AUTHN_LOGIN=<user/host identity> \
  -DCONJUR_AUTHN_API_KEY=<user/host API key - see notes about `CONJUR_AUTHN_URL`> \
  -Dexec.mainClass="com.myorg.client.App"
```
```java
import com.cyberark.conjur.api.Conjur;

// Configured using system properties
Conjur conjur = new Conjur();
// or using custom SSLContext setup as conjurSSLContext variable
Conjur conjur = new Conjur(conjurSSLContext);
```

### Username and Password

```bash
export CONJUR_ACCOUNT=<account specified during Secrets Manager setup>
export CONJUR_APPLIANCE_URL=<Secrets Manager endpoint URL>
```

```java
import com.cyberark.conjur.api.Conjur;

// Authenticate using provided username/hostname and password/API key. See notes about
// `CONJUR_AUTHN_URL` regarding how 'password-or-api-key' is processed.
Conjur conjur = new Conjur('host/host-id', 'password-or-api-key');
// or
Conjur conjur = new Conjur('username', 'password-or-api-key');
// or using custom SSLContext setup as conjurSSLContext variable
Conjur conjur = new Conjur('username', 'password-or-api-key', conjurSSLContext);
```

### Credentials

```bash
export CONJUR_ACCOUNT=<account specified during Secrets Manager setup>
export CONJUR_APPLIANCE_URL=<Secrets Manager endpoint URL>
```

```java
import com.cyberark.conjur.api.Conjur;
import com.cyberark.conjur.api.Credentials;

// Authenticate using a Credentials object. See notes about `CONJUR_AUTHN_URL`
// regarding how 'password-or-api-key' is processed.
Credentials credentials = new Credentials('username', 'password-or-api-key');
Conjur conjur = new Conjur(credentials);
// or using custom SSLContext setup as conjurSSLContext variable
Conjur conjur = new Conjur(credentials, conjurSSLContext);
```

### Authorization Token

```bash
export CONJUR_ACCOUNT=<account specified during Secrets Manager setup>
export CONJUR_APPLIANCE_URL=<Secrets Manager endpoint URL>
# Optional path for non-standard authenticators (e.g. `$CONJUR_APPLIANCE_URL/authn-k8s/myauthenticator`)
# export CONJUR_AUTHN_URL="<authenticator authn url>"
```

```java
import com.cyberark.conjur.api.Conjur;
import com.cyberark.conjur.api.Token;

Token token = Token.fromFile(Paths.get('path/to/conjur/authentication/token.json'));
Conjur conjur = new Conjur(token);
// or using custom SSLContext setup as conjurSSLContext variable
Conjur conjur = new Conjur(token, conjurSSLContext);
```

Alternatively, use the `CONJUR_AUTHN_TOKEN_FILE` environment variable:
```bash
export CONJUR_ACCOUNT=<account specified during Secrets Manager setup>
export CONJUR_APPLIANCE_URL=<Secrets Manager endpoint URL>
# Optional path for non-standard authenticators (e.g. `$CONJUR_APPLIANCE_URL/authn-k8s/myauthenticator`)
# export CONJUR_AUTHN_URL="<authenticator authn url>"
export CONJUR_AUTHN_TOKEN_FILE="path/to/conjur/authentication/token.json"
```
```java
import com.cyberark.conjur.api.Conjur;
import com.cyberark.conjur.api.Token;

Token token = Token.fromEnv();
Conjur conjur = new Conjur(token);
// or using custom SSLContext setup as conjurSSLContext variable
Conjur conjur = new Conjur(token, conjurSSLContext);
```

## Client APIs

To use the client, you will first create an instance of the client and then call methods
to send requests to the Secrets Manager API. The most common use case is adding and retrieving
a secret from Secrets Manager, so we provide some sample code for this use case below.

### Secrets Manager Client Instance (`com.cyberark.conjur.api.Conjur`)

The client can be instantiated with any of these methods:
```java
Conjur client = Conjur();
Conjur client = Conjur(SSLContext sslContext);
Conjur client = Conjur(String username, String password);
Conjur client = Conjur(String username, String password, SSLContext sslContext);
Conjur client = Conjur(String username, String password, String authnUrl);
Conjur client = Conjur(String username, String password, String authnUrl, SSLContext sslContext);
Conjur client = Conjur(Credentials credentials);
Conjur client = Conjur(Credentials credentials, SSLContext sslContext);
Conjur client = Conjur(Token token);
Conjur client = Conjur(Token token, SSLContext sslContext);
```

_Note:_ **As mentioned before, if you use the default `CONJUR_AUTHN_URL` value or your
`CONJUR_AUTHN_URL` ends with `/authn`, the `password` parameter is treated as a "password"
otherwise `CONJUR_AUTHN_API_KEY` is treated as an "API key".**

### Variables (`client.variables()`)

#### `void addSecret(String variableId, String secret)`

Sets a variable to a specific value based on its ID.

Example:
```java
import com.cyberark.conjur.api.Conjur;

Conjur conjur = new Conjur();
conjur.variables().addSecret(VARIABLE_ID, VARIABLE_VALUE);
```

_NOTE:_ For a variable to be set, it first needs to be created by a policy in Secrets Manager
otherwise this operation will fail. To do so, you will need a policy that resembles
the one supplied in the [Configuration](#configuration) section above.

#### `String retrieveSecret(String variableId)`

Retireves a variable based on its ID.

Example:
```java
import com.cyberark.conjur.api.Conjur;

Conjur conjur = new Conjur();
String secret = conjur.variables().retrieveSecret("<VARIABLE_ID>");
```

## Jakarta REST (JAX-RS) Implementations
The Secrets Manager API client uses the Jakarta REST (formerly JAX-RS) standard to make requests to the Secrets Manager web services.
It is compatible with Jakarta EE environments and may not work in Java EE environments that still use the
older javax.ws.rs packages.

Secrets Manager API uses Jersey as the default Jakarta REST implementation for client requests. While it is broadly compatible,
some application servers (e.g., JBoss EAP or WildFly) may require overriding the Jersey dependency in `pom.xml` to
avoid conflicts.

## Troubleshooting

### `error: package com.cyberark.conjur does not exist`

This is caused by Maven's (or your dependency resolution tooling) inability to find Secrets Manager
APIs. Please ensure that you have followed the [setup](#setup) section to properly install
this as a dependency.

### `java.lang.NoClassDefFoundError: javax/xml/bind/JAXBException`

This is due to the lack of dependencies required for this API. You can add this to your `pom.xml`
to work around this:
```xml
    <dependency>
      <groupId>javax.xml.bind</groupId>
      <artifactId>jaxb-api</artifactId>
      <version>2.3.1</version>
    </dependency>
```

### SSL/TLS/Certificate Issues

If you don't properly install the Secrets Manager certificate into the Java keystore, you may encounter
the folowing errors:
- `org.apache.cxf.interceptor.Fault: Could not send Message.`
- `jakarta.ws.rs.ProcessingException: javax.net.ssl.SSLHandshakeException: SSLHandshakeException`
- `javax.net.ssl.SSLHandshakeException: SSLHandshakeException`
- `javax.net.ssl.SSLHandshakeException: PKIX path building failed`
- `sun.security.validator.ValidatorException: PKIX path building failed`
- `sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target`

If you encounter these errors, please ensure that you have followed [this section](#set-up-trust-between-app-and-conjur)
on how to install Secrets Manager's SSL cetificate into your Java keystore correctly. You should also
ensure that the SSL certificate was added to the correct `cacerts` file if you have multiple
JDKs/JREs installed.

## Contributing

For instructions on how to contribute, please see our [Contributing](https://github.com/cyberark/conjur-api-java/blob/main/CONTRIBUTING.md)
guide.

## License

This repository is licensed under Apache License 2.0 - see [`LICENSE`](LICENSE) for more details.
