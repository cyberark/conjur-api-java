Conjur API for Java
===================
Java API SDK for Conjur OSS and Conjur Appliance. This SDK allows customers and developers to build new Java apps that communicate with Conjur, adding and retrieving resources for application use as needed.

## Table of Contents
- [Building](#building)
- [Setup](#setup)
- [Configuration](#configuration)

### Building

To build the library, you'll need Maven.  You can build the project (skipping the test build) like so:

```bash
git clone {repo}

cd conjur-api-java

mvn package -DskipTests

```

### Setup
To facilitate communication between the Java application and Conjur using the Conjur Java API, the following setup steps belows.

1. Create new maven project using an IDE of your choice
2. If you are using Maven to manage your project's dependencies, include the following Conjur API dependency in your `pom.xml`: 

```xml
<dependency>
  <groupId>net.conjur.api</groupId>
  <artifactId>conjur-api</artifactId>
  <version>2.1.0</version>
</dependency>
```

_NOTE:_ Depending on what version of the Java compiler you have, you may need to update the version. At this time, the {version} most compatible is `1.6`:
```xml
<properties>
    <maven.compiler.source>{version}</maven.compiler.source>
    <maven.compiler.target>{version}</maven.compiler.target>
</properties>
```

3. Run `mvn install` to install packages and their dependencies locally.

If you aren't using Maven, you can add the `jar` in the normal way. This `jar` can be found in
the `target` directory created when you ran `mvn package`.

### Configuration
Once the setup steps have been success, we will now make the connection between the new Java application and Conjur. The best way to do so is by setting environment variables. 
In Conjur (both OSS and Enterprise), environment variables are mapped to configuration variables by prepending `CONJUR_` to the all-caps name of the
configuration variable. For example, `appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT` etc.  

The following environment variables need to be included in the apps runtime environment in order use the Conjur API: CONJUR_ACCOUNT, CONJUR_AUTHN_LOGIN, CONJUR_AUTHN_API_KEY & CONJUR_APPLIANCE_URL.

CONJUR_ACCOUNT - account specified during Conjur setup
CONJUR_APPLIANCE_URL - Conjur HTTPS endpoint (OSS/Appliance)
CONJUR_AUTHN_LOGIN - user/host identity
CONJUR_AUTHN_API_KEY - user/host API key

For example, specify the environment variables like so:

```bash
CONJUR_ACCOUNT=myorg
CONJUR_AUTHN_LOGIN=host/myhost.example.com
CONJUR_AUTHN_API_KEY=sb0ncv1yj9c4w2e9pb1a2s
CONJUR_APPLIANCE_URL=https://conjur.myorg.com/api
```

_NOTE:_ Defining environment variables is dependent on where the application is running. For example, if the Java app is running in Docker, you would set them in `docker-compose.yml`.
If the app is running locally, you can configure them in a IDE.

In order to set CONJUR_AUTHN_LOGIN and CONJUR_AUTHN_API_KEY, you will need to add the host to Conjur with the proper privileges in policy. 
If you have already done so, you can skip the next set of steps.

1. Make sure Conjur is running in the background and open the Conjur CLI.
    
    _NOTE:_ If using Docker: `docker-compose exec <NAME_OF_CLI_CONTAINER> bash`

2. Copy the following policy, substitute the name of the policy, hostname, and permissions according to deemed necessary, and save it as a `.yml` file. In this policy, we are defining our Java 
application as a host and granting permissions over some variable. This variable will later hold a secret value.

```xml
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

3. Load the policy into Conjur like so: `conjur policy load <NAME_OF_POLICY>.yml`

4. Once the policy is loaded (for the first time), an id and API key should be returned for the host. It should look something like:
```bash
Loaded policy 'root'
{
  "created_roles": {
    "cucumber:host:<POLICY_ID>/<NAME_OF_HOST>": {
      "id": "cucumber:host:<POLICY_ID>/<NAME_OF_HOST>",
      "api_key": "3scdjaz3aq90vn26avdv11s2mf2h2wnwfzw3cbgbja1jspzyc1rdzwqx"
    }
  },
  "version": 1
}
```

5. Set the id and API key as the environment variables CONJUR_AUTHN_LOGIN and CONJUR_AUTHN_API_KEY respectively. 
    _Note:_ In Conjur, it is the standard that hosts are referred to in terms of its full hostname. Therefore, when setting the environment variable, the host id should be prefixed with `host/`. For example, the `host/<NAME_OF_HOST>`,
not just `<NAME_OF_HOST>`.

6. Give the variable defined in the previous policy a value like so: `conjur variable values add <POLICY_ID>/<NAME_OF_VARIABLE>`

There you have it! Now you are all configured to start leveraging the Conjur Java API in your Java program.
 
### SSL Certificates
By default, the Conjur appliance generates and uses self-signed SSL certificates (Java-specific certificates known as cacerts). 
You'll need to configure your Java app to trust them. You can accomplish this by loading the Conjur certificate into the Java keystore.
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
All authorization options require the environment variables `CONJUR_ACCOUNT` and `CONJUR_APPLIANCE_URL` to be set:
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
Token token = Token.fromFile(Paths.get('path/to/conjur/authentication/token.json'));
Conjur conjur = new Conjur(token);
```

Alternatively, to use the `CONJUR_AUTHN_TOKEN_FILE` environment variable:
```bash
export CONJUR_AUTHN_TOKEN_FILE="path/to/conjur/authentication/token.json"
```
```java
Token token = Token.fromEnv();
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

Copyright 2016-2019 CyberArk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
