/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.retry.el;

import io.gravitee.common.http.HttpHeaders;
import io.gravitee.gateway.api.proxy.ProxyResponse;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class EvaluableProxyResponse {

    private final ProxyResponse response;

    public EvaluableProxyResponse(ProxyResponse response) {
        this.response = response;
    }

    public int getStatus() {
        return this.response.status();
    }

    public String getReason() {
        return this.response.reason();
    }

    public HttpHeaders getHeaders() {
        return this.response.headers();
    }
}
