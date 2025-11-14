## Overview
You can use the `retry` policy to replay requests when experiencing backend connection issues or if the response meets a given _condition_.

If the retry takes too long, relative to the `timeout` value, the request stops and returns status code `502`.

> **_NOTE:_** To replay a request with a payload, the gateway stores it in memory. We recommend you avoid applying it to requests with a large payload.



## Usage
A `502` response will be returned in the following cases:

* No response satisfies the condition after `maxRetries`
* Technical errors when calling the backend (for example, connection refused, timeout)



