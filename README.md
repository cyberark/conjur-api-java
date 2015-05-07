Conjur API for Java
===================

## Installing

### From Source

To build the library from source you'll need Maven.  You can build it like this:

```bash
git clone {repo}

cd conjur-api

mvn package

```

If you are using Maven to manage your project's dependencies, you can run `mvn install` to install the package locally, and then include following dependency in your `pom.xml`:

```xml
<dependency>
  <groupId>net.conjur.api</groupId>
  <artifactId>conjur-api</artifactId>
  <version>1.1</version>
</dependency>
```

If you aren't using Maven (lucky you!), you can just add the `jar` in the normal way.  This `jar` can be found in
the `target` directory created when you ran `mvn package`.


Note that this will *not* run the integration tests, since these require access to a Conjur instance.  To run the
integration tests, you will need to define the following environment variables for the `mvn package` command:
```bash
CONJUR_INTEGRATION_TESTS_ENABLED=true
CONJUR_CREDENTIALS="username:password"
CONJUR_ACCOUNT="your-account"
CONJUR_STACK="your-stack"
```

## Basic Usage

### Specifying Service Endpoints

You will need the DNS name of the machine on which your Conjur appliance is installed.  
For the sake of this example we'll say that it's `"conjur.yourcompany.com"`.

Your `applianceUrl` is then `"https://conjur.yourcompany.com/api`".  Notice that this is an *https* address and that the
`/api` path is required.  You can get an `Endpoints` instance for the appliance like this:

```java
// In real life you probably read this from some sort of configuration file.
String applianceUrl = "https://conjur.yourcompany.com/api";
Endpoints applianceEndpoints = Endpoints.getApplianceEndpoints(applianceUrl);
```

If you want to use specific, custom URLs for some reason, you can use the 
`Endpoints.of(String authnUrl, String authzUrl, String directoryUrl)` factory method.  


### SSL Certificates

By default, the Conjur appliance generates and uses self-signed SSL certificates. You'll need to configure
Java to trust them. You can accomplish this by loading the Conjur certificate into 
the Java keystore.
First, you'll need a copy of this certificate, which you can get using the [Conjur CLI](linklink).  Once you've 
installed the command line tools, you can run

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

### Creating a Conjur Instance

A `Conjur` instance provides access to the individual Conjur services.  To create one, you'll need an `Endpoints` instance
as described above, as well as your Conjur login and password (which are configured during appliance setup or provided by 
your Conjur administrator).  You will typically create a `Conjur` instance from these values like this:

```java
String applianceUrl = "...";
String login = "..."; // your conjur username, not your account
String password = "...";
Endpoints endpoints = Endpoints.getApplianceEndpoints(applianceUrl);

Conjur conjur = new Conjur(login, password, endpoints);
```

## User Operations

You can perform operations on Conjur users with the `Users` class.  You do not create instances of this class directly,
but get them from the `users()` method on a `Conjur` instance.  
a
You can currently create users (with the `create` method) and check for their existence (with the `exists` method) of the
`Users` class.



## Variable Operations

Conjur variables can be created, fetched and deleted using the `Variables` service, which you get from `Conjur`'s `variables()`
method.

You can create variables with the various `create` methods, and fetch existing variables by id with the `get(String id)`
method.

Variable objects support all the methods available through the Conjur HTTP API for variables.  See the 
[Conjur API docs](http://developer.conjur.net/reference/services/directory/variable) for details on the variable methods.

## JAXRS Implementations

The Conjur API client uses the JAXRS standard to make requests to the Conjur web services.  In the future we plan to remove this dependency, but for the time being you may need to change the JAXRS implementation to conform to your environment and application dependencies.  For example, in a JBoss server environment, you should use the RESTlet implementation.  The Conjur API uses Apache CFX by default.  You can replace that dependency in `pom.xml` to use an alternative implementation.
