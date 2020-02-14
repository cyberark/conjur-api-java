# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## 2.1.0 - 2018-08-24
### Added
- Adds support for token based authentication to support Kubernetes Authenticator

## [2.0.0] - 2018-07-12
### Added
- License updated to Apache v2 - [PR #8](https://github.com/cyberark/conjur-api-java/pull/8)

### Changed
- Authn tokens now use the new Conjur 5 format - [PR #21](https://github.com/cyberark/conjur-api-java/pull/21)
- Configuration change. When using environment variables, use `CONJUR_AUTHN_LOGIN` and `CONJUR_AUTHN_API_KEY` now instead of `CONJUR_CREDENTIALS` - https://github.com/cyberark/conjur-api-java/commit/60344308fc48cb5380c626e612b91e1e720c03fb

[Unreleased]: https://github.com/cyberark/conjur-api-java/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/cyberark/conjur-api-java/compare/v1.1.0...v2.0.0
