Conjur API for Java
===================

## Installation

### From Source

To build the library from source you'll need Maven.  You can build it like this:

```bash
git clone {repo}

cd conjur-api-java

mvn package -DskipTests

```

If you are using Maven to manage your project's dependencies, you can run `mvn install` to install the package locally, and then include following dependency in your `pom.xml`:

```xml
<dependency>
  <groupId>net.conjur.api</groupId>
  <artifactId>conjur-api</artifactId>
  <version>2.1.0</version>
</dependency>
```

If you aren't using Maven, you can add the `jar` in the normal way. This `jar` can be found in
the `target` directory created when you ran `mvn package`.

Note that we ran `mvn package` without running the integration tests, since these require access to a Conjur instance. You can run the
integration tests with `mvn package` once you finished with the configuration.

### Configuration

The simplest way to configure the Conjur API is by using environment variables, which is often a bit more convenient.
Environment variables are mapped to configuration variables by prepending `CONJUR_` to the all-caps name of the
configuration variable. For example, `appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT` etc.  

The following environment variables are mandatory for running the API: CONJUR_ACCOUNT, CONJUR_AUTHN_LOGIN, CONJUR_AUTHN_API_KEY & CONJUR_APPLIANCE_URL.

CONJUR_ACCOUNT - account specified during Conjur setup
CONJUR_APPLIANCE_URL - Conjur HTTPS endpoint
CONJUR_AUTHN_LOGIN - user/host identity
CONJUR_AUTHN_API_KEY - user/host API key

For example, specify the environment variables like this:

```bash
CONJUR_ACCOUNT=myorg
CONJUR_AUTHN_LOGIN=host/myhost.example.com
CONJUR_AUTHN_API_KEY=sb0ncv1yj9c4w2e9pb1a2s
CONJUR_APPLIANCE_URL=https://conjur.myorg.com/api
```

Note that if you are connecting as a host, the login should be prefixed with `host/`. For example: `host/myhost.example.com`,
not just `myhost.example.com`.

In addition, to run the integration tests you will need to load a Conjur policy. Save this file as `root.yml`:

```yaml
- !policy
  id: test
  body:
    - !variable
      id: testVariable
```

To load the policy, use the CLI command `conjur policy load root root.yml`

### SSL Certificates

By default, the Conjur appliance generates and uses self-signed SSL certificates. You'll need to configure
Java to trust them. You can accomplish this by loading the Conjur certificate into the Java keystore.
First, you'll need a copy of this certificate, which you can get using the [Conjur CLI](https://developer.conjur.net/cli).
Once you've installed the command line tools, you can run

```bash
conjur init
```

and enter the required information at the prompts.  This will save the certificate to a file like `"conjur-mycompany.pem"`
in your HOME directory.  Java doesn't deal with the *pem* format, so next you'll need to convert it to the *der* format:

```bash
openssl x509 -outform der -in conjur-yourcompany.pem -out conjur-yourcompany.der
```

Next, you'll need to locate your JRE home.   On my machine it's `/usr/lib/jvm/java-7-openjdk-amd64/jre/`.  We'll export
this path to $JRE_HOME for convenience. If the file `$JRE_HOME/lib/security/cacerts` doesn't exist (you might need to be
root to see it), you've got the wrong path for your JRE_HOME.  Once you've found it, you can add the appliance's cert
to your keystore like this:

```bash
keytool -import -alias conjur-youraccount -keystore "$JRE_HOME/lib/security/cacerts"  -file ./conjur-youraccount.der
```

## Examples

### Authorization Patterns
All authorization options require the environment variables `CONJUR_ACCOUNT` and `CONJUR_APPLIANCE_URL` are set:
```sh
export CONJUR_ACCOUNT=<account specified during Conjur setup>
export CONJUR_APPLIANCE_URL=<Conjur HTTPS endpoint>
```

A `Conjur` instance provides access to the individual Conjur services. To create one, you'll need the environment
variables as described above. You will typically create a Conjur instance from these values in the following way:

#### Environment Variables
```sh
# Additionally set the following environment variables:
export CONJUR_AUTHN_LOGIN=<user/host identity>
export CONJUR_AUTHN_API_KEY=<user/host API key>
```
```java
// Using environment variables
Conjur conjur = new Conjur();
```

#### Username and Password
```java
// Authenticate using provided username/hostname and password/API key
Conjur conjur = new Conjur('host/host-id', 'api-key');
Conjur conjur = new Conjur('username', 'password');
```

#### Credentials
```java
// Authenticate using a Credentials object
Credentials credentials = new Credentials('username', 'password');
Conjur conjur = new Conjur(credentials);
```

#### Authorization Token
```java
String authTokenString = new String(Files.readAllBytes(Paths.get('path/to/conjur/authentication/token.json')));
Token token = Token.fromJson(authTokenString);
Conjur conjur = new Conjur(token);
```

### Variable Operations

Conjur variables store encrypted, access-controlled data. The most common thing a variable stores is a secret.
A variable can have one or more (up to 20) secrets associated with it, and ordered in reverse chronological order.

You will typically add secrets to variables & retrieve secrets from variables in the following way:

```java
conjur.variables().addSecret(VARIABLE_KEY, VARIABLE_VALUE);
String retrievedSecret = conjur.variables().retrieveSecret(VARIABLE_KEY);
```

## JAX-RS Implementations

The Conjur API client uses the JAX-RS standard to make requests to the Conjur web services.  In the future we plan to
remove this dependency, but for the time being you may need to change the JAX-RS implementation to conform to your
environment and application dependencies.  For example, in a JBoss server environment, you should use the RESTlet
implementation.  The Conjur API uses Apache CXF by default.  You can replace that dependency in `pom.xml` to use an
alternative implementation.

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## License

Copyright 2016-2018 CyberArk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
