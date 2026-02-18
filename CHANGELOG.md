# [4.1.0](https://github.com/gravitee-io/gravitee-policy-retry/compare/4.0.0...4.1.0) (2026-02-18)


### Features

* enable for A2A proxy ([5781f1c](https://github.com/gravitee-io/gravitee-policy-retry/commit/5781f1c85b36c02d2d4fff9020aadf9eb9e552a8))

# [4.0.0](https://github.com/gravitee-io/gravitee-policy-retry/compare/3.1.0...4.0.0) (2025-11-14)


### Features

* attach the cause to execution failure when retry has failed ([a877727](https://github.com/gravitee-io/gravitee-policy-retry/commit/a87772745bc906affc62bb4198206c16ad7c2349))


### BREAKING CHANGES

* Requires APIM 4.9

# [3.1.0](https://github.com/gravitee-io/gravitee-policy-retry/compare/3.0.1...3.1.0) (2025-11-12)


### Features

* enable for LLM & MCP Proxy API ([8b3a37b](https://github.com/gravitee-io/gravitee-policy-retry/commit/8b3a37bb93a8aa2d5ebb23fcf91d0f657fb69dd9))

## [3.0.1](https://github.com/gravitee-io/gravitee-policy-retry/compare/3.0.0...3.0.1) (2025-06-06)


### Bug Fixes

* properly resume request when retrying ([996e8e2](https://github.com/gravitee-io/gravitee-policy-retry/commit/996e8e286e85e4aced98d5ee2ec152a2bdc3a113))

# [3.0.0](https://github.com/gravitee-io/gravitee-policy-retry/compare/2.1.3...3.0.0) (2025-05-12)


### Features

* support for reactive engine ([c4c44fc](https://github.com/gravitee-io/gravitee-policy-retry/commit/c4c44fc45e1da3d8549c263531932d559afa322e))


### BREAKING CHANGES

* require at least APIM 4.6

## [2.1.3](https://github.com/gravitee-io/gravitee-policy-retry/compare/2.1.2...2.1.3) (2023-07-20)


### Bug Fixes

* update policy description ([21a75cc](https://github.com/gravitee-io/gravitee-policy-retry/commit/21a75cc22eb756f8eefb97f1c57a22eda1155eb6))

## [2.1.2](https://github.com/gravitee-io/gravitee-policy-retry/compare/2.1.1...2.1.2) (2022-05-30)


### Bug Fixes

* fix retry counter start value and previous response canceling ([6cffe6e](https://github.com/gravitee-io/gravitee-policy-retry/commit/6cffe6e550a783331ec54e26e25cea5abb0e3487))

## [2.1.1](https://github.com/gravitee-io/gravitee-policy-retry/compare/2.1.0...2.1.1) (2022-05-10)


### Bug Fixes

* assign policy to the 'others' category ([d9ef4f0](https://github.com/gravitee-io/gravitee-policy-retry/commit/d9ef4f0172bee78a6455e1389ac703f53c353436))

# [2.1.0](https://github.com/gravitee-io/gravitee-policy-retry/compare/2.0.0...2.1.0) (2022-01-21)


### Features

* **headers:** Internal rework and introduce HTTP Headers API ([6d530f7](https://github.com/gravitee-io/gravitee-policy-retry/commit/6d530f7cd33a67fa3c83d9a7d02e203c322d8ec8)), closes [gravitee-io/issues#6772](https://github.com/gravitee-io/issues/issues/6772)

# [2.0.0](https://github.com/gravitee-io/gravitee-policy-retry/compare/[secure]...2.0.0) (2021-12-20)


### Bug Fixes

* **oom:** close circuitbreaker and cleanup classloader ([6f1f8ad](https://github.com/gravitee-io/gravitee-policy-retry/commit/6f1f8ad245edb50dfbabc2c6362e03d011eb5653)), closes [gravitee-io/issues#6684](https://github.com/gravitee-io/issues/issues/6684)


### chore

* bump `gravitee-parent` and introduce `gravitee-bom` ([7615774](https://github.com/gravitee-io/gravitee-policy-retry/commit/7615774cea43d38e2341d8b3dbc560403b43723a))


### BREAKING CHANGES

* As this plugin is now relying on Vert.x 4.x, it can only be used with APIM 3.10+.
