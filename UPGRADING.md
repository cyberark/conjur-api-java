# Upgrading Conjur Java API

This guide describes how to upgrade your project to use Conjur Java API.

The main scenario covered in this document is migrating a project with a Conjur-Java-API
to v3.0.0.

For more details about using the Conjur API, or contributing to development, please
refer to the [Conjur Java API](https://github.com/cyberark/conjur-api-java).

For further assistance installing and configuring the Conjur Java API,
please refer to the [Setup](README.md#Setup) section of
the Conjur Java API [README.md](README.md) file.

## Migrating to 3.0.0

With the update to v.3.0.0, the Conjur Java API now makes use of the `com.cyberark` 
project namespace. This allows us to publish artifacts to this namespace, which are
immediately available to use in project. As such, your project can make use of the 
Conjur Java API without needing to build the Jarfile locally.

### Changes to your code base
Due to the change to the project namespace, we have modified the package name from 
`net.conjur.api` to `com.cyberark.conjur.api`. As such, all import statements must be
updated to reflect this. 

### Example
Before:
```java
import net.conjur.api.AuthnProvider
``` 

After:
```java
import com.cyberark.conjur.api.AuthnProvider
``` 

### Changes to your `pom.xml`
Due to the change to the project namespace, we have modified the package name from 
`net.conjur.api` to `com.cyberark.conjur.api`. As such, your dependency configuration
must be updated to reflect this. 

### Example
Before:
```xml
<dependency>
  <groupId>net.conjur.api</groupId>
  <artifactId>conjur-api</artifactId>
  <version>2.2.0</version>
<dependency>
```

After:
```xml
<dependency>
  <groupId>com.cyberark.conjur.api</groupId>
  <artifactId>conjur-api</artifactId>
  <version>3.0.0</version>
<dependency>
```
