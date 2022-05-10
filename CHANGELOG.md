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
