Conjur API for Java
===================

## Installing

### From Source

To build the library from source you'll need Maven.  You can build it like this:

```bash
git clone {repo}

cd conjur-api

mvn release
```

## Basic Usage

### Specifying Service Endpoints

The `Endpoints` class contains the URLs for the Conjur services.  If you happen to know exactly what these URLs are,
for example if you were running the services locally for development purposes, you can use the 
`Endpoints.of(String authnUrl, String authzUrl, String directoryUrl)` factory method.  

In most cases however, you will be using either hosted Conjur or appliance Conjur.  For hosted Conjur, we will 
provide you with an *account* and a *stack*.  These values can be used to get an `Endpoints` instance like this:

```java
String account = "my-account-name";
String stack = "v4"; // typically the stack corresponds to a version of the Conjur API.
Endpoints hostedEndpoints = Endpoints.getHostedEndpoints(account, stack);
```

For appliance Conjur, you will need the hostname of the machine on which your Conjur appliance is installed.  This will
often be the public DNS of an EC2 instance, but for the sake of this example we'll say that it's `"conjur.yourcompany.com"`.
Your `applianceUrl` is then `"https://conjur.yourcompany.com/api`".  Notice that this is an *https* address and that the
`/api` path is required.  You can get an `Endpoints` instance for the appliance like this:

```java
// In real life you probably read this from some sort of configuration file.
String applianceUrl = "https://conjur.yourcompany.com/api";
Endpoints applianceEndpoints = Endpoints.getApplianceEndpoints(applianceUrl);
```

### SSL Certificates

For hosted Conjur, the Conjur services use CA issued SSL certs, so you don't need to t anything special in order for 
the Java API to be able to connect to them.  However, for appliance Conjur, you will typically be using a self signed 
certificate generated when the appliance is configured.  Java needs this certificate in it's keystore in order to 
connect to the appliance.
First, you'll need a copy of this certificate, which you can get using the [Conjur CLI](linklink).  Once you've 
installed the command line tools, you can run

```bash
conjur init
```

and enter the required information at the prompts.  This will save the certificate to a file like `"conjur-mycompany.pem"`
in the current directory.  Java doesn't deal with the *pem* format, so next you'll need to convert it to the *der* format:

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
but get them from the `users()` method on a `Conjur` instance.  More, more...

## Variable Operations

TODO