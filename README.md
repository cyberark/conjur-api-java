Conjur API for Java
===================
Programmatic Java access to the Conjur API (for both Conjur OSS and Enterprise/DAP versions). This Java SDK allows developers to build new apps in Java that communicate with Conjur by invoking our Conjur API to perform operations on stored data (add, retrieve, etc).

## Table of Contents
- [Prequisites](#prerequisites)
- [Setup](#setup)
- [Configuration](#configuration)
- [Setup Trust Between App and Conjur](#setup-trust-between-app-and-conjur)
- [Authorization Examples](#authorization-examples)
- [Conjur Services Operation Examples](#conjur-services-operation-examples)
- [JAX-RS Implementations](#jax-rs-implementations)
- [Contributing](#contributing)
- [License](#license)

## Prerequisites
It is assumed that Conjur (OSS or Enterprise/DAP) and the Conjur CLI have already been installed in the environment and running in the background. If you haven't done so, follow these instructions for installation of the [OSS](https://docs.conjur.org/Latest/en/Content/OSS/Installation/Install_methods.htm) and these for installation of [Enterprise/DAP](https://docs.cyberark.com/Product-Doc/OnlineHelp/AAM-DAP/Latest/en/Content/Deployment/platforms/platforms.html).

Once Conjur and the Conjur CLI are running in the background, you are ready to start setting up your Java app to work with our Conjur Java API!

## Setup
You can grab the library's dependencies from the source by using Maven **or** locally by generating a JAR file and adding it to the project manually. 
 
To do so from the source using Maven, following the setup steps belows: 

1. Create new maven project using an IDE of your choice
2. If you are using Maven to manage your project's dependencies, include the following Conjur API dependency in your `pom.xml`: 

```xml
<dependency>
  <groupId>net.conjur.api</groupId>
  <artifactId>conjur-api</artifactId>
  <version>2.1.0</version>
</dependency>
```

_NOTE:_ Depending on what version of the Java compiler you have, you may need to update the version. At this time, the `{version}` most compatible is `1.8`:

```xml
<properties>
    <maven.compiler.source>{version}</maven.compiler.source>
    <maven.compiler.target>{version}</maven.compiler.target>
</properties>
```

3. Run `mvn install` to install packages and their dependencies locally.

If generating a JAR is preferred, you can build the library locally and add the dependency to the project manually by following the setup steps below:

1. Clone the Conjur Java API repo locally: `git clone {repo}`
2. `cd conjur-api-java`
3. Run `mvn package -DskipTests` to generate a JAR file. These files should be in the `target` directory of the repo
    
    _NOTE:_ we ran `mvn package` without running the integration tests, since these require access to a Conjur instance. You can run the integration tests with mvn package once you finished with the configuration. For more information on how to run the tests, take a look at our [Contributing](https://github.com/cyberark/conjur-api-java/blob/master/CONTRIBUTING.md) guide.

4. Follow the steps outlined [here](https://www.jetbrains.com/help/idea/library.html) for information on how to add JAR files into the new app's project files using Intellij and [here](https://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.wst.webtools.doc.user%2Ftopics%2Ftwplib.html) for Eclipse

## Configuration
Once the setup steps have been successfully run, we will now define the environment variables needed to make the connection between the new app and Conjur. The best way to do so is by setting environment variables. 
In Conjur (both OSS and DAP), environment variables are mapped to configuration variables by prepending `CONJUR_` to the all-caps name of the
configuration variable. For example, `appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT` etc.  

_NOTE:_ For ways to set credentials (for `CONJUR_AUTHN_LOGIN` and `CONJUR_AUTHN_API_KEY` environment variables ONLY) in the app instead of environment variables, see the [Authorization Examples](#authorization-examples) section below.

The following environment variables need to be included in the apps runtime environment in order use the Conjur API:
- `CONJUR_ACCOUNT`
- `CONJUR_AUTHN_LOGIN`
- `CONJUR_AUTHN_API_KEY`
- `CONJUR_APPLIANCE_URL`

`CONJUR_ACCOUNT` - account specified during Conjur setup

`CONJUR_APPLIANCE_URL` - Conjur HTTPS endpoint (OSS/DAP)

`CONJUR_AUTHN_LOGIN` - user/host identity

`CONJUR_AUTHN_API_KEY` - user/host API key

For example, specify the environment variables like so:

```bash
CONJUR_ACCOUNT=myorg
CONJUR_AUTHN_LOGIN=host/myhost.example.com
CONJUR_AUTHN_API_KEY=sb0ncv1yj9c4w2e9pb1a2s
CONJUR_APPLIANCE_URL=https://conjur.myorg.com/api
```

In order to set `CONJUR_AUTHN_LOGIN` and `CONJUR_AUTHN_API_KEY`, you will need to add the host to Conjur with the proper privileges in policy. 

For this _specific_ example, we will define the Java app as a host and grant permissions over some variable. This variable will later hold a secret and we will 
show how to retrieve this secret using the Conjur Java API once a connection is established. If you have a different use case, you can use the below policy as the basis for 
another desired outcome. If a policy already exists for your host, you can skip the next set of steps.

1. Make sure Conjur is running in the background and open the Conjur CLI.
    
    _NOTE:_ If using Docker: `docker-compose exec <NAME_OF_CLI_CONTAINER> bash`

2. Copy the following policy, substitute: the name of the policy, hostname, and permissions accordingly, and save it as a `.yml` file.

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

_NOTE:_ If you cannot remember the admin's API key and you have the proper privileges, you can run `conjurctl role retrieve-key cucumber:user:admin` from the Conjur server to retrieve the admin's API key and load the policy

3. Load the policy into Conjur using the CLI like so: `conjur policy load <PATH_TO_POLICY> <NAME_OF_POLICY>.yml`

_NOTE:_ For this policy, `<PATH_TO_POLICY>` is root. Meaning, this policy has been loaded into the root node of the policy tree. You may want to change this. For more information about the Conjur policy tree, check out [this](https://www.conjur.org/blog/understanding-conjur-policy/) blog post.

4. Once the policy is loaded (for the first time), an ID and API key will be returned for the host. It should look something like:
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

5. Set the ID and the API key returned by the previous command as the environment variables `CONJUR_AUTHN_LOGIN` and `CONJUR_AUTHN_API_KEY` respectively.
    
    _NOTE:_ In Conjur, it is the standard that hosts are referred to in terms of their full qualifier and type. Therefore, when setting value for the host's environment 
    variable, the host ID should be prefixed with `host/`. For example, the `host/<NAME_OF_HOST>`, not just `<NAME_OF_HOST>`.

6. Give the credential variable defined in the previous policy a value like so: `conjur variable values add <POLICY_ID>/<NAME_OF_VARIABLE> <VARIABLE_VALUE>`

Now that we have identified our host in Conjur, we now need the app to trust Conjur. To do this, the Conjur self-signed SSL certificate needs to be added to Java's certificate authority keystore.  

## Setup Trust Between App and Conjur
By default, the Conjur appliance generates and uses self-signed SSL certificates (Java-specific certificates known as cacerts). 
You'll need to configure your app to trust them. You can accomplish this by loading the Conjur certificate into Java's CA keystore that holds the list of all the allowed certificates for https connections.

First, you'll need a copy of this certificate, which you can get using the [Conjur CLI](https://developer.conjur.net/cli).
Run the following steps from the Conjur CLI to initialize Conjur in order to get Conjur certificates:

```bash
conjur init
```

and enter the required information at the prompts (URL of Conjur service and account name).  This will save (to your HOME directory) the certificate to a file like `conjur-default.pem` where the `default` suffix will be equal to your Conjur's account name.  Since Java doesn't work natively with the `pem` certificate encoding format, you'll need to convert it to the `der` format:

```bash
openssl x509 -outform der -in <PEM_PATH_IN_CLI/conjur-default.pem> -out conjur-default.der
```

Next, you'll need to locate the path to the JRE from the process environment running the Java app. In the case of Java 8 on most standard Linux distributions 
it's `/usr/lib/jvm/java-8-openjdk-amd64/jre`.  We'll export this path to $JRE_HOME for convenience. If the file `$JRE_HOME/lib/security/cacerts` doesn't exist (you might need to be root to see it), double check that the JRE_HOME path is correct. Once you've found it, you can add the appliance's cert to Java's certificate authority keystore like this:

```bash
keytool -import -alias conjur-default -keystore "$JRE_HOME/lib/security/cacerts"  -file ./conjur-default.der
```

There you have it! Now you are all configured to start leveraging the Conjur Java API in your Java program.

## Authorization Examples
As mentioned in the [Configuration](#configuration) section, in order to make the connection between the Java app and Conjur, environment variables `CONJUR_ACCOUNT` and `CONJUR_APPLIANCE_URL` need to be set:

```sh
export CONJUR_ACCOUNT=<account specified during Conjur setup>
export CONJUR_APPLIANCE_URL=<Conjur HTTPS endpoint>
```

but this isn't the only way. In addition to explicitly setting these environment variables, you can do so by providing values, using the Credentials object, or providing an Authorization Token)
Once you have chosen from one of the patterns below that works for you, you can now create a `Conjur` instance from these values to access Conjur services and make RESTful API calls.

### Environment Variables (the standard way)
```sh
# Additionally set the following environment variables:
export CONJUR_AUTHN_LOGIN=<user/host identity>
export CONJUR_AUTHN_API_KEY=<user/host API key>
```
```java
// Using environment variables
Conjur conjur = new Conjur();
```

### Username and Password
```java
// Authenticate using provided username/hostname and password/API key
Conjur conjur = new Conjur('host/host-id', 'api-key');
Conjur conjur = new Conjur('username', 'password');
```

### Credentials
```java
// Authenticate using a Credentials object
Credentials credentials = new Credentials('username', 'password');
Conjur conjur = new Conjur(credentials);
```

### Authorization Token
```java
Token token = Token.fromFile(Paths.get('path/to/conjur/authentication/token.json'));
Conjur conjur = new Conjur(token);
```

Alternatively, use the `CONJUR_AUTHN_TOKEN_FILE` environment variable:
```bash
export CONJUR_AUTHN_TOKEN_FILE="path/to/conjur/authentication/token.json"
```
```java
Token token = Token.fromEnv();
Conjur conjur = new Conjur(token);
```

## Conjur Services Operation Examples
This section provides some example operations on how invoke the Conjur Java API in your app. 

The most common use case is adding and retrieving a secret from Conjur. You can add secrets by invoking our API in the following way: 

```java
conjur.variables().addSecret(VARIABLE_KEY, VARIABLE_VALUE);
```

Alternatively, you can add a value to a secret in the Conjur CLI like so: 

`conjur variable values add <NAME_OF_POLICY/VARIABLE> <VALUE_OF_SECRET>`. 

Example: `conjur variable values add policyA/secret-var superSecret`

_NOTE:_ For a variable to be set, it first needs to be created by a policy in Conjur otherwise this operation will fail. To do so, you will need a policy that resembles the one supplied in the [Configuration](#configuration) section above.

To retrieve this secret:
```java
conjur.variables().retrieveSecret("<POLICY_PATH/VAR_NAME>");
``` 

Example:
```java
conjur.variables().retrieveSecret("app/test-var");
```

## JAX-RS Implementations

The Conjur API client uses the JAX-RS standard to make requests to the Conjur web services.  In the future we plan to
remove this dependency, but for the time being you may need to change the JAX-RS implementation to conform to your
environment and application dependencies.  For example, in a JBoss server environment, you should use the RESTlet
implementation.  The Conjur API uses Apache CXF by default.  You can replace that dependency in `pom.xml` to use an
alternative implementation.

## Contributing
For instructions on how to contribute, please see our [Contributing](https://github.com/cyberark/conjur-api-java/blob/master/CONTRIBUTING.md) guide.

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
