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
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.proxy.ProxyResponse;
import io.gravitee.gateway.api.stream.WriteStream;

/**
 * This Response wrapper is intented to be used with an {@link io.gravitee.gateway.api.el.EvaluableResponse}.
 * /!\ This is a trick to avoid creating a subclass of {@link io.gravitee.gateway.api.el.EvaluableResponse} in the classloader of the RetryPolicy
 * because, when passed to the EL TemplateEngine, the class will be put in cache and never released.
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ProxyResponseWrapper implements Response {

    private final ProxyResponse response;

    public ProxyResponseWrapper(ProxyResponse response) {
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

    @Override
    public Response status(int i) {
        // Useless has this response is read-only
        return this;
    }

    @Override
    public int status() {
        return response.status();
    }

    @Override
    public String reason() {
        return response.reason();
    }

    @Override
    public Response reason(String s) {
        // Useless has this response is read-only
        return this;
    }

    @Override
    public HttpHeaders headers() {
        return response.headers();
    }

    @Override
    public boolean ended() {
        return false;
    }

    @Override
    public HttpHeaders trailers() {
        return response.trailers();
    }

    @Override
    public WriteStream<Buffer> write(Buffer buffer) {
        // Useless has this response is read-only
        return null;
    }

    @Override
    public void end() {}
}
