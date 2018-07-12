# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0](https://github.com/cyberark/conjur-api-java/releases/tag/v2.0.0) - 2018-7-12
### Added
- License updated to Apache v2 - [PR #8](https://github.com/cyberark/conjur-api-java/pull/8)
### Changed
- Authn tokens now use the new Conjur 5 format - [PR #21](https://github.com/cyberark/conjur-api-java/pull/21)
- Configuration change. When using environment variables, use `CONJUR_AUTHN_LOGIN` and `CONJUR_AUTHN_API_KEY` now
    instead of `CONJUR_CREDENTIALS` - https://github.com/cyberark/conjur-api-java/commit/60344308fc48cb5380c626e612b91e1e720c03fb

## [1.4.0] - 2015-5-15
### Changed
- Remove non-JAXRS usage to allow users to provide their own implementation of the standard.
- Handle JSON with Gson

## [1.3.0] - 2015-4-16
### Changed
- Change variable behavior to reflect the fact that you may not have 'read' permission on 
a variable that you can 'execute' or 'update'.
- Allow SSL hostname verification to be disabled in order to facilitate development and debugging.

## [1.2.0] - 2015-4-15
### Changed
- Fix appliance endpoints bug
- deprecate Endpoints.of
- expose Endpoints constructors.

## [0.9.0] - 2018-7-11
### Changed
- Adds CIDR restrictions to Host and User resources
- Adds Kubernete authentication
- Optimize audit database and responses, for a significant improvement of performance.
- `start` no longer fails to show Help information.
