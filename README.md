
<!-- GENERATED CODE - DO NOT ALTER THIS OR THE FOLLOWING LINES -->
# Retry

[![Gravitee.io](https://img.shields.io/static/v1?label=Available%20at&message=Gravitee.io&color=1EC9D2)](https://download.gravitee.io/#graviteeio-apim/plugins/policies/gravitee-policy-retry/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/gravitee-io/gravitee-policy-retry/blob/master/LICENSE.txt)
[![Releases](https://img.shields.io/badge/semantic--release-conventional%20commits-e10079?logo=semantic-release)](https://github.com/gravitee-io/gravitee-policy-retry/releases)
[![CircleCI](https://circleci.com/gh/gravitee-io/gravitee-policy-retry.svg?style=svg)](https://circleci.com/gh/gravitee-io/gravitee-policy-retry)

## Overview
You can use the `retry` policy to replay requests when experiencing backend connection issues or if the response meets a given _condition_.

If the retry takes too long, relative to the `timeout` value, the request stops and returns status code `502`.

> **_NOTE:_** To replay a request with a payload, the gateway stores it in memory. We recommend you avoid applying it to requests with a large payload.



## Usage
A `502` response will be returned in the following cases:

* No response satisfies the condition after `maxRetries`
* Technical errors when calling the backend (for example, connection refused, timeout)




## Phases
The `retry` policy can be applied to the following API types and flow phases.

### Compatible API types

* `PROXY`
* `MCP PROXY`
* `LLM PROXY`

### Supported flow phases:

* Request

## Compatibility matrix
Strikethrough text indicates that a version is deprecated.

| Plugin version| APIM| Java version |
| --- | --- | ---  |
|2.x|4.5.x and earlier|17 |
|3.x|4.6.x to 4.8.x|17 |
|4.x|4.9.x and later|21 |


## Configuration options


#### 
| Name <br>`json name`  | Type <br>`constraint`  | Mandatory  | Default  | Description  |
|:----------------------|:-----------------------|:----------:|:---------|:-------------|
| Retry condition<br>`condition`| string| ✅| | The condition to check to retry or not the request (supports EL)|
| Delay (ms)<br>`delay`| integer<br>`[0, +Inf]`|  | `0`| The delay between each attempt|
| Send last attempt response<br>`lastResponse`| boolean|  | | Returns the last attempt response, even if it's a failure.|
| Max attempts<br>`maxRetries`| integer<br>`[1, +Inf]`| ✅| `1`| Number of attempts before failing (502 - Bad Gateway)|
| Timeout (ms)<br>`timeout`| integer<br>`[0, +Inf]`| ✅| `1000`| Consider a failure if a retry attempt does not succeed in time|




## Examples

*Retry when response code is 5xx*
```json
{
  "api": {
    "definitionVersion": "V4",
    "type": "PROXY",
    "name": "Retry example API",
    "flows": [
      {
        "name": "Common Flow",
        "enabled": true,
        "selectors": [
          {
            "type": "HTTP",
            "path": "/",
            "pathOperator": "STARTS_WITH"
          }
        ],
        "request": [
          {
            "name": "Retry",
            "enabled": true,
            "policy": "retry",
            "configuration":
              {
                  "condition": "{#response.status > 500}",
                  "maxRetries": 3,
                  "timeout": 1000
              }
          }
        ]
      }
    ]
  }
}

```
*Specifying a delay between retries*
```json
{
  "api": {
    "definitionVersion": "V4",
    "type": "PROXY",
    "name": "Retry example API",
    "flows": [
      {
        "name": "Common Flow",
        "enabled": true,
        "selectors": [
          {
            "type": "HTTP",
            "path": "/",
            "pathOperator": "STARTS_WITH"
          }
        ],
        "request": [
          {
            "name": "Retry",
            "enabled": true,
            "policy": "retry",
            "configuration":
              {
                  "condition": "{#response.status > 500}",
                  "maxRetries": 3,
                  "delay": 1000,
                  "timeout": 1000
              }
          }
        ]
      }
    ]
  }
}

```
*Returns the last attempt response, even if it's a failure*
```json
{
  "api": {
    "definitionVersion": "V4",
    "type": "PROXY",
    "name": "Retry example API",
    "flows": [
      {
        "name": "Common Flow",
        "enabled": true,
        "selectors": [
          {
            "type": "HTTP",
            "path": "/",
            "pathOperator": "STARTS_WITH"
          }
        ],
        "request": [
          {
            "name": "Retry",
            "enabled": true,
            "policy": "retry",
            "configuration":
              {
                  "condition": "{#response.status > 500}",
                  "maxRetries": 3,
                  "timeout": 1000,
                  "lastResponse": true
              }
          }
        ]
      }
    ]
  }
}

```


## Changelog

### [3.1.0](https://github.com/gravitee-io/gravitee-policy-retry/compare/3.0.1...3.1.0) (2025-11-12)


##### Features

* enable for LLM & MCP Proxy API ([8b3a37b](https://github.com/gravitee-io/gravitee-policy-retry/commit/8b3a37bb93a8aa2d5ebb23fcf91d0f657fb69dd9))

#### [3.0.1](https://github.com/gravitee-io/gravitee-policy-retry/compare/3.0.0...3.0.1) (2025-06-06)


##### Bug Fixes

* properly resume request when retrying ([996e8e2](https://github.com/gravitee-io/gravitee-policy-retry/commit/996e8e286e85e4aced98d5ee2ec152a2bdc3a113))

### [3.0.0](https://github.com/gravitee-io/gravitee-policy-retry/compare/2.1.3...3.0.0) (2025-05-12)


##### Features

* support for reactive engine ([c4c44fc](https://github.com/gravitee-io/gravitee-policy-retry/commit/c4c44fc45e1da3d8549c263531932d559afa322e))


##### BREAKING CHANGES

* require at least APIM 4.6

#### [2.1.3](https://github.com/gravitee-io/gravitee-policy-retry/compare/2.1.2...2.1.3) (2023-07-20)


##### Bug Fixes

* update policy description ([21a75cc](https://github.com/gravitee-io/gravitee-policy-retry/commit/21a75cc22eb756f8eefb97f1c57a22eda1155eb6))

#### [2.1.2](https://github.com/gravitee-io/gravitee-policy-retry/compare/2.1.1...2.1.2) (2022-05-30)


##### Bug Fixes

* fix retry counter start value and previous response canceling ([6cffe6e](https://github.com/gravitee-io/gravitee-policy-retry/commit/6cffe6e550a783331ec54e26e25cea5abb0e3487))

#### [2.1.1](https://github.com/gravitee-io/gravitee-policy-retry/compare/2.1.0...2.1.1) (2022-05-10)


##### Bug Fixes

* assign policy to the 'others' category ([d9ef4f0](https://github.com/gravitee-io/gravitee-policy-retry/commit/d9ef4f0172bee78a6455e1389ac703f53c353436))

### [2.1.0](https://github.com/gravitee-io/gravitee-policy-retry/compare/2.0.0...2.1.0) (2022-01-21)


##### Features

* **headers:** Internal rework and introduce HTTP Headers API ([6d530f7](https://github.com/gravitee-io/gravitee-policy-retry/commit/6d530f7cd33a67fa3c83d9a7d02e203c322d8ec8)), closes [gravitee-io/issues#6772](https://github.com/gravitee-io/issues/issues/6772)

### [2.0.0](https://github.com/gravitee-io/gravitee-policy-retry/compare/[secure]...2.0.0) (2021-12-20)


##### Bug Fixes

* **oom:** close circuitbreaker and cleanup classloader ([6f1f8ad](https://github.com/gravitee-io/gravitee-policy-retry/commit/6f1f8ad245edb50dfbabc2c6362e03d011eb5653)), closes [gravitee-io/issues#6684](https://github.com/gravitee-io/issues/issues/6684)


##### chore

* bump `gravitee-parent` and introduce `gravitee-bom` ([7615774](https://github.com/gravitee-io/gravitee-policy-retry/commit/7615774cea43d38e2341d8b3dbc560403b43723a))


##### BREAKING CHANGES

* As this plugin is now relying on Vert.x 4.x, it can only be used with APIM 3.10+.

