/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.retry;

import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.RequestWrapper;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.handler.Handler;
import io.gravitee.gateway.api.stream.ReadStream;

/**
 * The retry request is defined to store the incoming request body into a buffer which would be reusable in case
 * of retry to an other endpoint.
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
class RetryRequest extends RequestWrapper {

    private Buffer buffer;
    private boolean retry;
    private Handler<Buffer> bodyHandler;
    private Handler<Void> endHandler;
    private boolean retryBuffer;

    RetryRequest(Request request) {
        super(request);
    }

    /**
     * Marks whether the request is a retry or not.
     * <p>
     * If set to {@code true}, the cached body can be sent to the backend,
     * allowing the request to resume consumption from where it left off.
     *
     * @param retry {@code true} to enable retry with cached body, {@code false} otherwise.
     */
    public void markRetry(boolean retry) {
        this.retry = retry;
        this.retryBuffer = retry;
    }

    @Override
    public ReadStream<Buffer> bodyHandler(Handler<Buffer> bodyHandler) {
        this.bodyHandler = bodyHandler;

        request.bodyHandler(result -> {
            if (buffer == null) {
                buffer = Buffer.buffer();
            }
            buffer.appendBuffer(result);
            bodyHandler.handle(result);
        });

        return this;
    }

    @Override
    public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;

        request.endHandler(endHandler);
        return this;
    }

    /**
     * Resumes the processing of the HTTP request body stream.
     * <p>
     * If the request is not marked for retry, this simply resumes the underlying request stream.
     * <p>
     * In case of a retry, this method ensures that any cached request body (which may be partially read)
     * is first delivered to the body handler before resuming or completing the request:
     * <ul>
     *   <li>If a cached buffer exists and is eligible to be replayed, it is passed to the body handler.</li>
     *   <li>If the request body has already been fully consumed, the {@code endHandler} is invoked directly.</li>
     *   <li>Otherwise, the request stream is resumed to continue processing remaining content.</li>
     * </ul>
     */
    @Override
    public ReadStream<Buffer> resume() {
        if (!retry) {
            request.resume();
        } else {
            // This is a retry. We first need to send the cached request body needed (could be partial, if the whole request body hasn't been consumed).
            if (bodyHandler != null && buffer != null && retryBuffer) {
                bodyHandler.handle(buffer);
                retryBuffer = false;
            }

            if (request.ended()) {
                // The request body is entirely consumed, we must call the endHandler.
                endHandler.handle(null);
            } else {
                // There are remaining thing to consume on the request, resuming.
                request.resume();
            }
        }

        return this;
    }
}
