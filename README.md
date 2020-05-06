Conjur API for Java
===================
Programmatic Java access to the Conjur API (for both Conjur OSS and Enterprise/DAP versions).
This Java SDK allows developers to build new apps in Java that communicate with Conjur by
invoking our Conjur API to perform operations on stored data (add, retrieve, etc).

## Table of Contents

- [Prequisites](#prerequisites)
- [Setup](#setup)
- [Configuration](#configuration)
- [Setup Trust Between App and Conjur](#setup-trust-between-app-and-conjur)
- [Authorization Examples](#authorization-examples)
- [Client APIs](#client-apis)
- [JAX-RS Implementations](#jax-rs-implementations)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Prerequisites

It is assumed that Conjur (OSS or Enterprise/DAP) and the Conjur CLI have already been
installed in the environment and running in the background. If you haven't done so,
follow these instructions for installation of the [OSS](https://docs.conjur.org/Latest/en/Content/OSS/Installation/Install_methods.htm)
and these for installation of [Enterprise/DAP](https://docs.cyberark.com/Product-Doc/OnlineHelp/AAM-DAP/Latest/en/Content/Deployment/platforms/platforms.html).

Once Conjur and the Conjur CLI are running in the background, you are ready to start
setting up your Java app to work with our Conjur Java API!

## Setup

### Using the Source Code

You can grab the library's dependencies from the source by using Maven **or** locally
by generating a JAR file and adding it to the project manually.

To do so from the source using Maven, following the setup steps below:

1. Create new maven project using an IDE of your choice
2. If you are using Maven to manage your project's dependencies, include the following
   Conjur API dependency snippet in your `pom.xml` under `<project>`/`<dependencies>`:

```xml
    <dependency>
      <groupId>net.conjur.api</groupId>
      <artifactId>conjur-api</artifactId>
      <version>2.2.0</version>
    <dependency>
```

_NOTE:_ Depending on what version of the Java compiler you have, you may need to update
the version. At this time, the `{version}` most compatible is `1.8`:

```xml
  <properties>
    <maven.compiler.source>{version}</maven.compiler.source>
    <maven.compiler.target>{version}</maven.compiler.target>
  </properties>
```

3. Run `mvn install -DskipTests -Dmaven.javadoc.skip=true` in this repo's directory to install
Conjur API into your local maven repository.

### Using the Jarfile

If generating a JAR is preferred, you can build the library locally and add the dependency
to the project manually by following the setup steps below:

1. Clone the Conjur Java API repo locally: `git clone {repo}`
2. Go into the cloned repository with `cd conjur-api-java`
3. Run `mvn package -DskipTests -Dmaven.javadoc.skip=true` to generate a JAR file. The output
   `.jar` files will be located in the `target` directory of the repo

_NOTE:_ we ran `mvn package` without running the integration tests, since these require access
to a Conjur instance. You can run the integration tests with mvn package once you finished with
the configuration. For more information on how to run the tests, take a look at our
[Contributing](https://github.com/cyberark/conjur-api-java/blob/master/CONTRIBUTING.md) guide.

4. Follow the steps outlined [here](https://www.jetbrains.com/help/idea/library.html) for
   information on how to add JAR files into the new app's project files using Intellij and
   for Eclipse you can do:
   `Right click project > Build Path > Configure Build Path > Library > Add External JARs`.
5. If you are working with the CLI, you can manually install the `.jar` into your Maven
   repository by running the following but replacing `$VERSION` with the appropriate version
   of the API:
   ```sh-session
   $ mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file \
       -Dfile=/path/to/api/repo/target/conjur-api-$VERSION.jar

   $ # or

   $ mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file \
       -Dfile=/path/to/api/repo/target/conjur-api-$VERSION-with-dependencies.jar

   $ or
   $ mvn install:install-file -Dfile=/path/to/api/repo/target/conjur-api-$VERSION-with-dependencies.jar \
       -DgroupId=net.conjur.api \
       -DartifactId=conjur-api \
       -Dversion=$VERSION \
       -Dpackaging=jar
   ```

## Configuration

Once the setup steps have been successfully run, we will now define the variables needed
to make the connection between the new app and Conjur. You can do this by setting
[environment variables](#environment-variables), [system properties](#system-properties),
or some combination of both.

_NOTE:_ System properties will override enviroment values when both are defined for a
variable.

### Environment Variables

In Conjur (both OSS and DAP), environment variables are mapped to configuration variables
by prepending `CONJUR_` to the all-caps name of the configuration variable. For example,
`appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT` etc.

_NOTE:_ For ways to set credentials (for `CONJUR_AUTHN_LOGIN` and `CONJUR_AUTHN_API_KEY`
environment variables ONLY) in the app instead of environment variables, see the
[Authorization Examples](#authorization-examples) and/or [system properties](#system-properties)
section below.

The following environment variables need to be included in the apps runtime environment in
order use the Conjur API:

- `CONJUR_APPLIANCE_URL` - Conjur endpoint (OSS/DAP) in URL format.
- `CONJUR_ACCOUNT` - Conjur account that you are connecting to. This value is set during Conjur deployment.
- `CONJUR_AUTHN_LOGIN` - User/host identity
- `CONJUR_AUTHN_API_KEY` - User/host API key
- `CONJUR_AUTHN_URL` - (optional) Alternate authn endpoint. Default endpoint uses `<applianceUrl>/authn`
  for generic username and password/API key login flow.

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
If you are using a host-based user like this example shows, you will need to add the host
to Conjur with the proper privileges in policy in order to know the appropriate
`CONJUR_AUTHN_LOGIN` and `CONJUR_AUTHN_API_KEY` values.

For this _specific_ example, we will define the Java app as a host and grant permissions over
some variable. This variable will later hold a secret and we will show how to retrieve this
secret using the Conjur Java API once a connection is established. If you have a different use
case, you can use the below policy as the basis for another desired outcome. If a policy
already exists for your host, you can skip the next set of steps.

1. Make sure Conjur is running in the background and open the Conjur CLI.

   _NOTE:_ If using Docker: `docker-compose exec <NAME_OF_CLI_CONTAINER> bash`

2. Copy the following policy and substitute `<>`-enclosed variables accordingly, and
   save it as a `.yml` file.
   ```yaml
- !policy
  id: <POLICY_ID>
  body:
    - !host
      id: <NAME_OF_HOST>

    - !variable
      id: <NAME_OF_VARIABLE>

    - !permit
      role: !host <NAME_OF_HOST>
      privileges: [read, execute]
      resource: !variable <NAME_OF_VARIABLE>
   ```

   _NOTE:_ If you are running Conjur OSS, cannot remember the admin's API key, and you
   have the proper privileges, you can run `conjurctl role retrieve-key <CONJUR_ACCOUNT>:user:admin`
   from the Conjur server container to retrieve the admin's API key.

3. Load the policy into Conjur using the CLI:
   ```sh-session
   $ conjur policy load <PATH_TO_POLICY> <NAME_OF_POLICY>.yml
   ```

   _NOTE:_ For this policy, `<PATH_TO_POLICY>` is root. Meaning, this policy has been
   loaded into the root node of the policy tree. You may want to change this depending
   on your policy tree. For more information about the Conjur policy tree, check out
   [this](https://www.conjur.org/blog/understanding-conjur-policy/) blog post.

4. Once the policy is loaded (for the first time), an ID and API key will be returned
   for the host. It should look something like:
   ```sh-session
$ conjur policy load root policy.yml
Loaded policy 'root'
{
  "created_roles": {
    "myorg:host:<POLICY_ID>/<NAME_OF_HOST>": {
      "id": "myorg:host:<POLICY_ID>/<NAME_OF_HOST>",
      "api_key": "3scdjaz3aq90vn26avdv11s2mf2h2wnwfzw3cbgbja1jspzyc1rdzwqx"
    }
  },
  "version": 1
}
   ```

5. Set the ID and the API key returned by the previous command as the environment variables
   `CONJUR_AUTHN_LOGIN` and `CONJUR_AUTHN_API_KEY` respectively.
   _NOTE:_ In Conjur, it is the standard that hosts are referred to in terms of their full
   qualifier and type. Therefore, when setting value for the host's environment variable,
   the host ID should be prefixed with `host/`. For example, the `host/POLICY_ID/NAME_OF_HOST`,
   not just `POLICY_ID/NAME_OF_HOST`.

6. Give the credential variable defined in the previous policy a value using the CLI:
   ```sh-session
   $ conjur variable values add <POLICY_ID>/<NAME_OF_VARIABLE> <VARIABLE_VALUE>
   ```

7. Now that we have identified our host in Conjur, we now also need the app to trust Conjur.
   To do this, the Conjur self-signed SSL certificate needs to be added to Java's certificate
   authority keystore. You can find more info on how to do this in the [Setup Trust](#setup-trust-between-app-and-conjur)
   section below.

### System Properties

This API can also be configured using [Java system properties](https://www.baeldung.com/java-system-get-property-vs-system-getenv).
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

_NOTE:_ When using properties to configure Conjur APIs, be careful not to persist sensitive
values (like the API key) in source-controlled property files!

## Setup Trust Between App and Conjur

By default, the Conjur appliance generates and uses self-signed SSL certificates (Java-specific
certificates known as cacerts). Without trusting them, your Java app will not be able to connect
to the Conjur server over APIs and so you will need to configure your app to trust them. You can
accomplish this by loading the Conjur certificate into Java's CA keystore that holds the list of
all the allowed certificates for https connections.

First, we need to get a copy of this certificate, which you can get using `openssl`. Run the
following step from a terminal with OpenSSL that has access to Conjur:

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

There you have it! Now you are all configured to start leveraging the Conjur Java API in
your Java program.

## Authorization Examples

As mentioned in the [Configuration](#configuration) section, you can provide varying ways
for your app to authenticate against a Conjur server. Generally environment variables are
most common but this isn't the only way. In addition to explicitly setting these environment
variables, you can do so by providing [properties](#system-properties), using the Credentials
object, or by providing an Authorization Token. Once you have chosen from one of the
patterns below that works for you, you can now create a `Conjur` class instance values to
access Conjur services and make RESTful API calls.

### Environment Variables

```bash
export CONJUR_ACCOUNT=<account specified during Conjur setup>
export CONJUR_APPLIANCE_URL=<Conjur endpoint URL>
export CONJUR_AUTHN_LOGIN=<user/host identity>
export CONJUR_AUTHN_API_KEY=<user/host API key>
```
```java
import net.conjur.api.Conjur;

// Using environment variables
Conjur conjur = new Conjur();
```

### System Properties

```sh-session
$ java -jar myConjurClient.jar \
     -DCONJUR_ACCOUNT=<account specified during Conjur setup> \
     -DCONJUR_APPLIANCE_URL=<Conjur endpoint URL> \
     -DCONJUR_AUTHN_LOGIN=<user/host identity> \
     -DCONJUR_AUTHN_API_KEY=<user/host API key>
```
```java
import net.conjur.api.Conjur;

// Using environment variables
Conjur conjur = new Conjur();
```

### System Properties with Maven

```sh-session
$ mvn exec:java \
  -DCONJUR_ACCOUNT=<account specified during Conjur setup> \
  -DCONJUR_APPLIANCE_URL=<Conjur endpoint URL> \
  -DCONJUR_AUTHN_LOGIN=<user/host identity> \
  -DCONJUR_AUTHN_API_KEY=<user/host API key> \
  -Dexec.mainClass="com.myorg.client.App"
```
```java
import net.conjur.api.Conjur;

// Using environment variables
Conjur conjur = new Conjur();
```

### Username and Password

```bash
export CONJUR_ACCOUNT=<account specified during Conjur setup>
export CONJUR_APPLIANCE_URL=<Conjur endpoint URL>
```

```java
import net.conjur.api.Conjur;

// Authenticate using provided username/hostname and password/API key
Conjur conjur = new Conjur('host/host-id', 'api-key');
// or
Conjur conjur = new Conjur('username', 'password');
```

### Credentials

```bash
export CONJUR_ACCOUNT=<account specified during Conjur setup>
export CONJUR_APPLIANCE_URL=<Conjur endpoint URL>
```

```java
import net.conjur.api.Conjur;
import net.conjur.api.Credentials;

// Authenticate using a Credentials object
Credentials credentials = new Credentials('username', 'password');
Conjur conjur = new Conjur(credentials);
```

### Authorization Token

```bash
export CONJUR_ACCOUNT=<account specified during Conjur setup>
export CONJUR_APPLIANCE_URL=<Conjur endpoint URL>
# Optional path for non-standard authenticators (e.g. `$CONJUR_APPLIANCE_URL/authn-k8s/myauthenticator`)
# export CONJUR_AUTHN_URL="<authenticator authn url>"
```

```java
import net.conjur.api.Conjur;
import net.conjur.api.Token;

Token token = Token.fromFile(Paths.get('path/to/conjur/authentication/token.json'));
Conjur conjur = new Conjur(token);
```

Alternatively, use the `CONJUR_AUTHN_TOKEN_FILE` environment variable:
```bash
export CONJUR_AUTHN_TOKEN_FILE="path/to/conjur/authentication/token.json"
```
```java
import net.conjur.api.Conjur;
import net.conjur.api.Token;

Token token = Token.fromEnv();
Conjur conjur = new Conjur(token);
```

## Client APIs

Most usage is done by creating a Client instance and then invoking the API on it. The
most common use case is adding and retrieving a secret from Conjur.

### Conjur Client Instance (`net.conjur.api.Conjur`)

The client can be instantiated with any of these methods:
```java
Conjur client = Conjur();
Conjur client = Conjur(String username, String password);
Conjur client = Conjur(String username, String password, String authnUrl);
Conjur client = Conjur(Credentials credentials);
Conjur client = Conjur(Token token);
```

### Variables (`client.variables()`)

#### `void addSecret(String variableId, String secret)`

Sets a variable to a specific value based on its ID.

Example:
```java
import net.conjur.api.Conjur;

Conjur conjur = new Conjur();
conjur.variables().addSecret(VARIABLE_ID, VARIABLE_VALUE);
```

_NOTE:_ For a variable to be set, it first needs to be created by a policy in Conjur
otherwise this operation will fail. To do so, you will need a policy that resembles
the one supplied in the [Configuration](#configuration) section above.

#### `String retrieveSecret(String variableId)`

Retireves a variable based on its ID.

Example:
```java
import net.conjur.api.Conjur;

Conjur conjur = new Conjur();
conjur.variables().retrieveSecret("<VARIABLE_ID>");
```

## JAX-RS Implementations

The Conjur API client uses the JAX-RS standard to make requests to the Conjur web services.
In the future we plan to remove this dependency, but for the time being you may need to
change the JAX-RS implementation to conform to your environment and application dependencies.
Conjur API uses Apache CXF by default but for example, in a JBoss server environment, you
should use the RESTlet implementation. You can replace that dependency in `pom.xml` to use an
alternative implementation.

## Troubleshooting

### `error: package net.conjur does not exist`

This is caused by Maven's (or your dependency resolution tooling) inability to find Conjur
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

If you don't properly install the Conjur certificate into the Java keystore, you may encounter
the folowing errors:
- `org.apache.cxf.interceptor.Fault: Could not send Message.`
- `javax.ws.rs.ProcessingException: javax.net.ssl.SSLHandshakeException: SSLHandshakeException`
- `javax.net.ssl.SSLHandshakeException: SSLHandshakeException`
- `javax.net.ssl.SSLHandshakeException: PKIX path building failed`
- `sun.security.validator.ValidatorException: PKIX path building failed`
- `sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target`

If you encounter these errors, please ensure that you have followed [this section](#setup-trust-between-app-and-conjur)
on how to install Conjur's SSL cetificate into your Java keystore correctly. You should also
ensure that the SSL certificate was added to the correct `cacerts` file if you have multiple
JDKs/JREs installed.

## Contributing

For instructions on how to contribute, please see our [Contributing](https://github.com/cyberark/conjur-api-java/blob/master/CONTRIBUTING.md)
guide.

## License

This repository is licensed under Apache License 2.0 - see [`LICENSE`](LICENSE) for more details.
