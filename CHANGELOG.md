# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Fixed
- Encode spaces to "%20" instead of "+". This encoding fixes an issue where Conjur
  variables that have spaces were not encoded correctly
  ([https://github.com/cyberark/conjur-api-java#78](https://github.com/cyberark/conjur-api-java/issues/78))

### Added
- Implemented [#74](https://github.com/cyberark/conjur-api-java/issues/74)
- Updated code to enable adding custom javax.net.ssl.SSLContext to Conjur which
  enables us to set up a trust between application and Conjur server from Java
  code
- README has been updated with example SSLContext setup and it's use in Conjur
  class constructors

## [2.2.1] - 2020-05-08
### Fixed
- README has been updated to reflect the correct/expected usage of this SDK ([#70](https://github.com/cyberark/conjur-api-java/issues/70),
  [#50](https://github.com/cyberark/conjur-api-java/issues/50),
  [#39](https://github.com/cyberark/conjur-api-java/issues/39),
  [#18](https://github.com/cyberark/conjur-api-java/issues/18))

## [2.2.0] - 2020-04-30
### Added
- Enabled supplying alternative authentication URLs when instantiating the client,
  which allows clients to use alternative Conjur authentication methods such as
  authn-iam and authn-oidc. ([cyberark/conjur-api-java#40](https://github.com/cyberark/conjur-api-java/issues/40))
- Maven pom.xml file includes fat jar creation to allow easy incorporation of
  this client ([PR cyberark/conjur-api-java#47](https://github.com/cyberark/conjur-api-java/issues/47))

### Fixed
- Updated code so that properties will be retrieved from system properties, and
  if not found will then be retrieved from environment variables. ([cyberark/conjur-api-java#17](https://github.com/cyberark/conjur-api-java/issues/17))
- If a mandatory property (`CONJUR_ACCOUNT`, `CONJUR_APPLIANCE_URL`) is not provided
  a more verbose exception message will be thrown instead of a `NullPointerException`.
  ([cyberark/conjur-api-java#41](https://github.com/cyberark/conjur-api-java/issues/41))
- Improved error handling for missing / undefined env properties that are
  mandatory for proper system functioning.
  ([PR cyberark/conjur-api-java#47](https://github.com/cyberark/conjur-api-java/issues/47))

## [2.1.0] - 2018-08-24
### Added
- Adds support for token based authentication to support Kubernetes Authenticator

## [2.0.0] - 2018-07-12
### Added
- License updated to Apache v2 - [PR #8](https://github.com/cyberark/conjur-api-java/pull/8)

### Changed
- Authn tokens now use the new Conjur 5 format - [PR #21](https://github.com/cyberark/conjur-api-java/pull/21)
- Configuration change. When using environment variables, use `CONJUR_AUTHN_LOGIN` and `CONJUR_AUTHN_API_KEY` now instead of `CONJUR_CREDENTIALS` - https://github.com/cyberark/conjur-api-java/commit/60344308fc48cb5380c626e612b91e1e720c03fb

[Unreleased]: https://github.com/cyberark/conjur-api-java/compare/v2.2.1...HEAD
[2.0.0]: https://github.com/cyberark/conjur-api-java/compare/v1.1.0...v2.0.0
[2.1.0]: https://github.com/cyberark/conjur-api-java/compare/v2.0.0...v2.1.0
[2.2.0]: https://github.com/cyberark/conjur-api-java/compare/v2.1.0...v2.2.0
[2.2.1]: https://github.com/cyberark/conjur-api-java/compare/v2.2.0...v2.2.1
