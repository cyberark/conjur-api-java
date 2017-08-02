# XDD Overview

## Problem
As a developer, how do I retrieve secrets from Conjur CE using Java?

## User Experience:

#### Assumptions
  * Conjur CE running, healthy and accessible
  * Policy loaded that provides permission for this application to access a secret
  * The machine running application been conjurized (it has an api key and host name)

#### Experience
As a Java developer who needs to retrieve a secret from Conjur CE, I'd like to experience the following:

```java
// config file will include the Conjur CE host name, api key, URL (https://conjur:3000), optionally a custom CA Cert (if no ca cert)

 Conjur.getInstance().getSecret('secret-name')
```

because it's simple to implement and understand and allows me to securely retrieve secrets.  

#### Future work:
* Important this implementation be thread safe
* Do we need to add additional functionality? (setting secrets/managing policy)

#### Steps:
- [ ] Verify existing project works
- [ ] Write new classes inside existing projects w/ simple tests
- [ ] Conjur CE setup for testing/developing
