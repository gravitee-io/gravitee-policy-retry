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
import java.util.ArrayList;
import java.util.List;

public class RetryReadStream implements ReadStream<Buffer> {

    private final ReadStream<Buffer> source;
    private final List<Buffer> cachedChunks = new ArrayList<>();

    private Handler<Buffer> bodyHandler;
    private Handler<Void> endHandler;

    private boolean ended = false;
    private boolean resumed = false;
    private boolean replaying = false;

    public RetryReadStream(ReadStream<Buffer> source) {
        this.source = source;
        wireHandlers();
    }

    private void wireHandlers() {
        source.bodyHandler(chunk -> {
            cachedChunks.add(chunk);
            if (!replaying && bodyHandler != null) {
                bodyHandler.handle(chunk);
            }
        });

        source.endHandler(v -> {
            ended = true;
            if (!replaying && endHandler != null) {
                endHandler.handle(null);
            }
        });
    }

    @Override
    public ReadStream<Buffer> bodyHandler(Handler<Buffer> handler) {
        this.bodyHandler = handler;
        return this;
    }

    @Override
    public ReadStream<Buffer> endHandler(Handler<Void> handler) {
        this.endHandler = handler;
        return this;
    }

    @Override
    public ReadStream<Buffer> pause() {
        source.pause();
        return this;
    }

    @Override
    public ReadStream<Buffer> resume() {
        if (!resumed) {
            resumed = true;
            source.resume();
        } else if (ended && !replaying) {
            replaying = true;
            for (Buffer chunk : cachedChunks) {
                if (bodyHandler != null) bodyHandler.handle(chunk);
            }
            if (endHandler != null) endHandler.handle(null);
        }
        return this;
    }

    public boolean hasEnded() {
        return ended;
    }
}
