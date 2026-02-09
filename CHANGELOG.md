# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [3.1.2] - 2026-02-09

### Added
- Added JaCoCo plugin for code coverage reporting

### Changed
- Updated to JDK 25

## [3.1.1] - 2025-09-11

### Changed
- Updated documentation to align with Conjur Enterprise name change to Secrets Manager. (CNJR-10972)

## [3.1.0] - 2025-03-27

### Added
- Added Telemetry Headers

### Fixed
- Restored compatiblity with Jakarta EE libraries

## [3.0.7] - 2025-03-25

## [3.0.6] - 2024-02-20

### Fixed
- Restored compatibility with Java 8 (CNJR-7854)

## [3.0.5] - 2023-06-08

### Fixed
- Fix dependency information stripped from non-shaded jar
  [cyberark/conjur-api-java#119](https://github.com/cyberark/conjur-api-java/issues/119)

### Security
- Update nginx to 1.24 in Dockerfile.nginx
  [cyberark/conjur-api-java#118](https://github.com/cyberark/conjur-api-java/issues/118)
- Update pom.xml dependencies (CONJSE-1839)

### Changed
- Migrate JAX-RS to latest Jakarta version
  [cyberark/conjur-api-java#119](https://github.com/cyberark/conjur-api-java/issues/119)
- Avoid calling `login` for host
  [cyberark/conjur-api-java#117](https://github.com/cyberark/conjur-api-java/pull/117)

## [3.0.4] - 2023-02-27

### Security
- Update Dockerfile base images, bump Apache cxf to 3.5.5
  [cyberark/conjur-api-java#113](https://github.com/cyberark/conjur-api-java/issues/113)

## [3.0.3] - 2022-05-31

### Security
- Upgraded OpenJDK Dockerfile base image to `17-jdk-bullseye`.
  [cyberark/conjur-api-java#107](https://github.com/cyberark/conjur-api-java/pull/107)
- Upgraded nginx Dockerfile base image to fix CVE-2022-0778 and CVE-2022-1292.
  [cyberark/conjur-api-java#111](https://github.com/cyberark/conjur-api-java/pull/111)

## [3.0.2] - 2020-10-28
### Fixed
- Multiple unused transitive dependencies, such as `exec-maven-plugin`, were removed. 
  These could cause issues with downstream projects that had the `conjur-api-java` as 
  a dependency [cyberark/conjur-api-java#93](https://github.com/cyberark/conjur-api-java/issues/93)

## [3.0.1] - 2020-06-23
### Fixed
- A minor syntax issue in the `pom.xml` did not meet the requirements for Maven Central 
  publishing, preventing `3.0.0` from being released on Maven Central. This requirement 
  has been added, and all versions from `3.0.1` onward will be released on Maven Central. 
  [PR cyberark/conjur-api-java#86](https://github.com/cyberark/conjur-api-java/pull/86)

## [3.0.0] - 2020-06-22
### Fixed
- Encode spaces to "%20" instead of "+". This encoding fixes an issue where Conjur
  variables that have spaces were not encoded correctly.
  [cyberark/conjur-api-java#78](https://github.com/cyberark/conjur-api-java/issues/78)

### Added
- The `conjur-api-java` is now available through Maven Central without needing to be 
  built locally. Please see our [README.md](./README.md#using-maven-releases) for more 
  information on how you can use our latest Maven releases in your project! Alternatively, 
  check out [UPGRADING.md](./UPGRADING.md) to find out how to upgrade to `3.0.0` 
  through maven central. [cyberark/conjur-api-java#6](https://github.com/cyberark/conjur-api-java/issues/6)
- Enabled setting custom `javax.net.ssl.SSLContext` for TLS connection to Conjur server,
  which enables users to set up trust between the app and Conjur directly from the Java code.
  [cyberark/conjur-api-java#74](https://github.com/cyberark/conjur-api-java/issues/74)
- Introduced [upgrade instructions](https://github.com/cyberark/conjur-api-java/UPGRADING.md) 
  to provide instructions for upgrading to 3.0.0, or make use of published artifacts. These can be 
  found in `UPGRADING.md`. [cyberark/conjur-api-java#77](https://github.com/cyberark/conjur-api-java/issues/77)

### Changed
- Package renamed from `net.conjur.api` to `com.cyberark.conjur.api`.
  [cyberark/conjur-api-java#6](https://github.com/cyberark/conjur-api-java/issues/6)

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

[Unreleased]: https://github.com/cyberark/conjur-api-java/compare/v3.1.2...HEAD
[3.1.2]: https://github.com/cyberark/conjur-api-java/compare/v3.1.1...v3.1.2
[3.1.1]: https://github.com/cyberark/conjur-api-java/compare/v3.1.0...v3.1.1
[3.1.0]: https://github.com/cyberark/conjur-api-java/compare/v3.0.7...v3.1.0
[3.0.7]: https://github.com/cyberark/conjur-api-java/compare/v3.0.6...v3.0.7
[3.0.6]: https://github.com/cyberark/conjur-api-java/compare/v3.0.5...v3.0.6
[3.0.5]: https://github.com/cyberark/conjur-api-java/compare/v3.0.4...v3.0.5
[3.0.4]: https://github.com/cyberark/conjur-api-java/compare/v3.0.3...v3.0.4
[3.0.3]: https://github.com/cyberark/conjur-api-java/compare/v3.0.2...v3.0.3
[3.0.2]: https://github.com/cyberark/conjur-api-java/compare/v3.0.1...v3.0.2
[3.0.1]: https://github.com/cyberark/conjur-api-java/compare/v3.0.0...v3.0.1
[3.0.0]: https://github.com/cyberark/conjur-api-java/compare/v2.2.1...v3.0.0
[2.0.0]: https://github.com/cyberark/conjur-api-java/compare/v1.1.0...v2.0.0
[2.1.0]: https://github.com/cyberark/conjur-api-java/compare/v2.0.0...v2.1.0
[2.2.0]: https://github.com/cyberark/conjur-api-java/compare/v2.1.0...v2.2.0
[2.2.1]: https://github.com/cyberark/conjur-api-java/compare/v2.2.0...v2.2.1
