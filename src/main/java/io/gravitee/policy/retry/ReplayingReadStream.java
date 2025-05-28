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

import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.handler.Handler;
import io.gravitee.gateway.api.stream.ReadStream;

public class ReplayingReadStream implements ReadStream<Buffer> {

    private final Buffer cachedBody;
    private final boolean bodyEnded;

    private Handler<Buffer> bodyHandler;
    private Handler<Void> endHandler;

    public ReplayingReadStream(Buffer cachedBody, boolean bodyEnded) {
        this.cachedBody = cachedBody;
        this.bodyEnded = bodyEnded;
    }

    @Override
    public ReadStream<Buffer> bodyHandler(Handler<Buffer> handler) {
        this.bodyHandler = handler;
        if (cachedBody != null && handler != null) {
            handler.handle(cachedBody);
        }
        return this;
    }

    @Override
    public ReadStream<Buffer> endHandler(Handler<Void> handler) {
        this.endHandler = handler;
        if (bodyEnded && handler != null) {
            handler.handle(null);
        }
        return this;
    }

    @Override
    public ReadStream<Buffer> pause() {
        return this;
    }

    @Override
    public ReadStream<Buffer> resume() {
        return this;
    }
}
