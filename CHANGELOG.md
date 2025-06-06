## [2.1.4](https://github.com/gravitee-io/gravitee-policy-retry/compare/2.1.3...2.1.4) (2025-06-06)


### Bug Fixes

* applied retry policy fix from master ([11b08a3](https://github.com/gravitee-io/gravitee-policy-retry/commit/11b08a366fe1e142036c254a4c00f72d35393820))
* testcase fix ([e0ec20f](https://github.com/gravitee-io/gravitee-policy-retry/commit/e0ec20f92f33069f188bb757ef5081a6fa9133ea))
* testcase fixes ([953a301](https://github.com/gravitee-io/gravitee-policy-retry/commit/953a30164160239439551a6f364fac9762ebc05d))

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
